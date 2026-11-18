package br.uff.banco.contas;

import static br.uff.banco.suporte.Cenarios.SENHA_TRANSACAO;
import static org.assertj.core.api.Assertions.assertThat;

import br.uff.banco.contas.application.NovaTransferencia;
import br.uff.banco.contas.application.OperacaoContaService;
import br.uff.banco.contas.application.TransferenciaService;
import br.uff.banco.contas.domain.SaldoInsuficienteException;
import br.uff.banco.shared.domain.Dinheiro;
import br.uff.banco.suporte.Cenarios;
import br.uff.banco.suporte.Cenarios.ClienteDeTeste;
import br.uff.banco.suporte.TesteDeIntegracao;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Operações simultâneas na mesma conta.
 * <p>
 * Legado: {@code atualizaSaldoAposOperacao} lia o saldo, somava em Java e regravava,
 * sem lock nem transação — duas operações ao mesmo tempo perdiam uma atualização.
 */
@TesteDeIntegracao
class ConcorrenciaIT {

    private static final int THREADS = 20;

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    OperacaoContaService operacoes;

    @Autowired
    TransferenciaService transferencias;

    @Test
    void saquesSimultaneosNuncaDeixamOSaldoNegativoNemPerdemAtualizacao() throws Exception {
        ClienteDeTeste cliente = new Cenarios(mvc).clienteComSaldo("100.00");
        long conta = cliente.corrente().id();

        // 20 saques de 10,00 contra 100,00 de saldo: exatamente 10 passam.
        List<Resultado> resultados = emParalelo(THREADS, i -> () ->
                operacoes.sacar(cliente.id(), conta, Dinheiro.of("10.00"), SENHA_TRANSACAO));

        assertThat(resultados.stream().filter(Resultado::sucesso).count()).isEqualTo(10);
        assertThat(resultados.stream().filter(r -> !r.sucesso()))
                .allMatch(r -> r.erro() instanceof SaldoInsuficienteException);
        assertThat(saldo(conta)).isEqualByComparingTo("0.00");
        assertThat(contar("SELECT COUNT(*) FROM saque WHERE conta_id = ?", conta)).isEqualTo(10);
        assertSaldoBateComLancamentos(conta);
    }

    @Test
    void transferenciasCruzadasSimultaneasNaoTravamEConservamODinheiro() throws Exception {
        Cenarios cenarios = new Cenarios(mvc);
        ClienteDeTeste a = cenarios.clienteComSaldo("1000.00");
        ClienteDeTeste b = cenarios.clienteComSaldo("1000.00");

        // A→B e B→A ao mesmo tempo: sem ordem de lock, isso dá deadlock no banco.
        List<Resultado> resultados = emParalelo(THREADS, i -> () -> {
            ClienteDeTeste origem = i % 2 == 0 ? a : b;
            ClienteDeTeste destino = i % 2 == 0 ? b : a;
            return transferencias.transferir(origem.id(), new NovaTransferencia(
                    UUID.randomUUID().toString(), origem.corrente().id(), destino.corrente().agencia(),
                    destino.corrente().numero(), destino.cpf(), Dinheiro.of("7.00"), SENHA_TRANSACAO));
        });

        assertThat(resultados).allMatch(Resultado::sucesso);
        // 10 de cada lado: os saldos voltam ao inicial e o total se conserva
        assertThat(saldo(a.corrente().id())).isEqualByComparingTo("1000.00");
        assertThat(saldo(b.corrente().id())).isEqualByComparingTo("1000.00");
        assertSaldoBateComLancamentos(a.corrente().id());
        assertSaldoBateComLancamentos(b.corrente().id());
    }

    @Test
    void mesmaChaveDeIdempotenciaEmParaleloDebitaUmaVezSo() throws Exception {
        Cenarios cenarios = new Cenarios(mvc);
        ClienteDeTeste origem = cenarios.clienteComSaldo("100.00");
        ClienteDeTeste destino = cenarios.clienteComContas();
        String chave = UUID.randomUUID().toString();

        List<Resultado> resultados = emParalelo(8, i -> () -> transferencias.transferir(origem.id(), new NovaTransferencia(
                chave, origem.corrente().id(), destino.corrente().agencia(), destino.corrente().numero(),
                destino.cpf(), Dinheiro.of("25.00"), SENHA_TRANSACAO)));

        // Uma executa; as outras recebem o comprovante (reenvio) ou esbarram na chave única (409).
        assertThat(resultados.stream().filter(r -> !r.sucesso()))
                .allMatch(r -> r.erro() instanceof DataIntegrityViolationException);
        assertThat(contar("SELECT COUNT(*) FROM transferencia WHERE chave_idempotencia = ?", chave)).isEqualTo(1);
        assertThat(saldo(origem.corrente().id())).isEqualByComparingTo("75.00");
        assertThat(saldo(destino.corrente().id())).isEqualByComparingTo("25.00");
    }

    private interface Tarefa {
        Callable<Object> criar(int indice);
    }

    private record Resultado(boolean sucesso, Throwable erro) {
    }

    /** Dispara as tarefas juntas (largada sincronizada) e coleta sucesso/erro de cada uma. */
    private static List<Resultado> emParalelo(int quantidade, Tarefa tarefa) throws Exception {
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Object>> futuros = new ArrayList<>();
        try (ExecutorService threads = Executors.newFixedThreadPool(quantidade)) {
            for (int i = 0; i < quantidade; i++) {
                Callable<Object> corpo = tarefa.criar(i);
                futuros.add(threads.submit(() -> {
                    largada.await();
                    return corpo.call();
                }));
            }
            largada.countDown();
            List<Resultado> resultados = new ArrayList<>();
            for (Future<Object> futuro : futuros) {
                try {
                    futuro.get(60, TimeUnit.SECONDS);
                    resultados.add(new Resultado(true, null));
                } catch (ExecutionException e) {
                    resultados.add(new Resultado(false, e.getCause()));
                }
            }
            return resultados;
        }
    }

    private BigDecimal saldo(long contaId) {
        return jdbc.queryForObject("SELECT saldo FROM conta WHERE id = ?", BigDecimal.class, contaId);
    }

    private int contar(String sql, Object... parametros) {
        return jdbc.queryForObject(sql, Integer.class, parametros);
    }

    private void assertSaldoBateComLancamentos(long contaId) {
        BigDecimal soma = jdbc.queryForObject("""
                SELECT COALESCE(SUM(CASE natureza WHEN 'CREDITO' THEN valor ELSE -valor END), 0)
                  FROM lancamento WHERE conta_id = ?
                """, BigDecimal.class, contaId);
        assertThat(saldo(contaId)).isEqualByComparingTo(soma);
    }
}
