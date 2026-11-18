package br.uff.banco.contas;

import static br.uff.banco.suporte.Cenarios.SENHA_TRANSACAO;
import static org.assertj.core.api.Assertions.assertThat;

import br.uff.banco.contas.application.ConsultaContaService;
import br.uff.banco.contas.application.ExtratoView;
import br.uff.banco.contas.application.LancamentoView;
import br.uff.banco.contas.application.NovaTransferencia;
import br.uff.banco.contas.application.OperacaoContaService;
import br.uff.banco.contas.application.TransferenciaService;
import br.uff.banco.contas.domain.FormaDeposito;
import br.uff.banco.contas.domain.Natureza;
import br.uff.banco.shared.domain.Dinheiro;
import br.uff.banco.shared.domain.RegraNegocioException;
import br.uff.banco.suporte.Cenarios;
import br.uff.banco.suporte.Cenarios.ClienteDeTeste;
import br.uff.banco.suporte.Cenarios.ContaDeTeste;
import br.uff.banco.suporte.TesteDeIntegracao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Critério de pronto da Fase 4: o saldo de cada conta é exatamente o que os
 * lançamentos dizem.
 * <p>
 * Em vez de casos escolhidos a dedo, gera uma sequência aleatória (semente fixa,
 * reproduzível) de depósitos, saques e transferências entre seis contas — inclusive
 * operações que precisam falhar por falta de saldo — e confere o invariante por
 * todos os ângulos.
 */
@TesteDeIntegracao
class InvarianteDoLedgerIT {

    private static final long SEMENTE = 20261118L;
    private static final int OPERACOES = 150;

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    OperacaoContaService operacoes;

    @Autowired
    TransferenciaService transferencias;

    @Autowired
    ConsultaContaService consultas;

    @Test
    void saldoDeCadaContaEhExatamenteOQueOsLancamentosDizem() throws Exception {
        Cenarios cenarios = new Cenarios(mvc);
        List<Participante> participantes = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ClienteDeTeste cliente = cenarios.clienteComContas();
            participantes.add(new Participante(cliente, cliente.corrente()));
            participantes.add(new Participante(cliente, cliente.poupanca()));
        }

        Random aleatorio = new Random(SEMENTE);
        BigDecimal totalDepositado = BigDecimal.ZERO;
        BigDecimal totalSacado = BigDecimal.ZERO;
        int recusadasPorSaldo = 0;

        for (int i = 0; i < OPERACOES; i++) {
            Participante p = participantes.get(aleatorio.nextInt(participantes.size()));
            Dinheiro valor = Dinheiro.of(BigDecimal.valueOf(1 + aleatorio.nextInt(50_000), 2)); // 0,01 a 500,00
            try {
                switch (aleatorio.nextInt(3)) {
                    case 0 -> {
                        operacoes.depositar(p.cliente().id(), p.conta().id(), valor, FormaDeposito.DINHEIRO, SENHA_TRANSACAO);
                        totalDepositado = totalDepositado.add(valor.valor());
                    }
                    case 1 -> {
                        operacoes.sacar(p.cliente().id(), p.conta().id(), valor, SENHA_TRANSACAO);
                        totalSacado = totalSacado.add(valor.valor());
                    }
                    default -> {
                        Participante destino = outro(participantes, p, aleatorio);
                        transferencias.transferir(p.cliente().id(), new NovaTransferencia(
                                UUID.randomUUID().toString(), p.conta().id(), destino.conta().agencia(),
                                destino.conta().numero(), destino.cliente().cpf(), valor, SENHA_TRANSACAO));
                    }
                }
            } catch (RegraNegocioException e) {
                assertThat(e.getCodigo()).as("só falta de saldo pode recusar uma operação aqui").isEqualTo("saldo-insuficiente");
                recusadasPorSaldo++;
            }
        }
        assertThat(recusadasPorSaldo).as("a sequência precisa exercitar recusas").isPositive();

        BigDecimal somaDosSaldos = BigDecimal.ZERO;
        for (Participante p : participantes) {
            long conta = p.conta().id();
            BigDecimal saldo = saldo(conta);
            somaDosSaldos = somaDosSaldos.add(saldo);

            // 1. saldo = Σ créditos − Σ débitos
            assertThat(saldo).as("conta %d: saldo × soma dos lançamentos", conta).isEqualByComparingTo(somaDosLancamentos(conta));
            // 2. nunca ficou negativo em nenhum momento
            assertThat(jdbc.queryForObject("SELECT MIN(saldo_apos) FROM lancamento WHERE conta_id = ?", BigDecimal.class, conta))
                    .as("conta %d: menor saldo_apos", conta).isNotNegative();
            // 3. cada saldo_apos = anterior ± valor (a cadeia não tem buracos)
            assertCadeiaDeSaldos(conta);
            // 4. o extrato, que lê do mesmo ledger por outro caminho, chega no mesmo saldo
            ExtratoView extrato = consultas.extrato(p.cliente().id(), conta, LocalDate.now(ZoneId.of("America/Sao_Paulo")).minusDays(1),
                    LocalDate.now(ZoneId.of("America/Sao_Paulo")), 0, 100);
            assertThat(extrato.saldoFinal()).isEqualByComparingTo(saldo);
            assertThat(extrato.saldoInicial().add(extrato.totalCreditos()).subtract(extrato.totalDebitos()))
                    .isEqualByComparingTo(extrato.saldoFinal());
        }
        // 5. transferências só movem dinheiro: o total do sistema = depósitos − saques
        assertThat(somaDosSaldos).isEqualByComparingTo(totalDepositado.subtract(totalSacado));
    }

    private void assertCadeiaDeSaldos(long conta) {
        List<LancamentoView> lancamentos = jdbc.query(
                "SELECT natureza, valor, saldo_apos FROM lancamento WHERE conta_id = ? ORDER BY ocorrido_em, id",
                (rs, n) -> new LancamentoView(null, null, null, Natureza.valueOf(rs.getString(1)), null, null,
                        rs.getBigDecimal(2), rs.getBigDecimal(3)),
                conta);
        BigDecimal esperado = BigDecimal.ZERO;
        for (LancamentoView l : lancamentos) {
            esperado = l.natureza() == Natureza.CREDITO ? esperado.add(l.valor()) : esperado.subtract(l.valor());
            assertThat(l.saldoApos()).as("conta %d: cadeia de saldo_apos", conta).isEqualByComparingTo(esperado);
        }
    }

    private static Participante outro(List<Participante> participantes, Participante origem, Random aleatorio) {
        Participante destino;
        do {
            destino = participantes.get(aleatorio.nextInt(participantes.size()));
        } while (destino.conta().id() == origem.conta().id());
        return destino;
    }

    private BigDecimal saldo(long conta) {
        return jdbc.queryForObject("SELECT saldo FROM conta WHERE id = ?", BigDecimal.class, conta);
    }

    private BigDecimal somaDosLancamentos(long conta) {
        return jdbc.queryForObject("""
                SELECT COALESCE(SUM(CASE natureza WHEN 'CREDITO' THEN valor ELSE -valor END), 0)
                  FROM lancamento WHERE conta_id = ?
                """, BigDecimal.class, conta);
    }

    private record Participante(ClienteDeTeste cliente, ContaDeTeste conta) {
    }
}
