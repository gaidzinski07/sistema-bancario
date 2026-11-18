package br.uff.banco.contas.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.uff.banco.agencias.domain.Agencia;
import br.uff.banco.clientes.domain.Cliente;
import br.uff.banco.clientes.domain.SegmentoCliente;
import br.uff.banco.identidade.domain.Usuario;
import br.uff.banco.shared.domain.Cpf;
import br.uff.banco.shared.domain.Dinheiro;
import br.uff.banco.shared.domain.RegraNegocioException;
import java.lang.reflect.Field;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Regras de saldo da conta e da transferência (sem banco). */
class MovimentacaoContaTest {

    private static final Instant AGORA = Instant.parse("2026-11-18T12:00:00Z");

    @Test
    void creditoAumentaSaldoEGeraLancamentoComSaldoApos() {
        Conta conta = novaConta();

        Lancamento lancamento = conta.creditar(movimento(TipoLancamento.DEPOSITO, "150.00"));

        assertThat(conta.getSaldo()).isEqualTo(Dinheiro.of("150.00"));
        assertThat(lancamento.getNatureza()).isEqualTo(Natureza.CREDITO);
        assertThat(lancamento.getValor()).isEqualTo(Dinheiro.of("150.00"));
        assertThat(lancamento.getSaldoApos()).isEqualTo(Dinheiro.of("150.00"));
        assertThat(lancamento.getConta()).isSameAs(conta);
    }

    @Test
    void debitoDiminuiSaldoEGeraLancamento() {
        Conta conta = contaComSaldo("100.00");

        Lancamento lancamento = conta.debitar(movimento(TipoLancamento.SAQUE, "40.00"));

        assertThat(conta.getSaldo()).isEqualTo(Dinheiro.of("60.00"));
        assertThat(lancamento.getNatureza()).isEqualTo(Natureza.DEBITO);
        assertThat(lancamento.getSaldoApos()).isEqualTo(Dinheiro.of("60.00"));
    }

    @Test
    void podeZerarOSaldo() {
        Conta conta = contaComSaldo("100.00");

        conta.debitar(movimento(TipoLancamento.SAQUE, "100.00"));

        assertThat(conta.getSaldo()).isEqualTo(Dinheiro.ZERO);
    }

    /** Legado: Realizarsaque subtraía do saldo sem conferir nada. */
    @Test
    void debitoMaiorQueOSaldoEhRecusadoESaldoNaoMuda() {
        Conta conta = contaComSaldo("100.00");

        assertThatThrownBy(() -> conta.debitar(movimento(TipoLancamento.SAQUE, "100.01")))
                .isInstanceOf(SaldoInsuficienteException.class)
                .extracting("codigo").isEqualTo("saldo-insuficiente");
        assertThat(conta.getSaldo()).isEqualTo(Dinheiro.of("100.00"));
    }

    /** Legado: valor negativo numa transferência "puxava" dinheiro do destinatário. */
    @ParameterizedTest
    @ValueSource(strings = {"0", "-0.01", "-500"})
    void movimentoComValorNaoPositivoEhRecusado(String valor) {
        assertThatThrownBy(() -> movimento(TipoLancamento.DEPOSITO, valor))
                .isInstanceOf(RegraNegocioException.class)
                .extracting("codigo").isEqualTo("valor-invalido");
    }

    @Test
    void contaBloqueadaNaoMovimenta() throws Exception {
        Conta conta = contaComSaldo("100.00");
        definir(conta, "status", StatusConta.BLOQUEADA);

        assertThatThrownBy(() -> conta.debitar(movimento(TipoLancamento.SAQUE, "1.00")))
                .extracting("codigo").isEqualTo("conta-nao-ativa");
        assertThatThrownBy(() -> conta.creditar(movimento(TipoLancamento.DEPOSITO, "1.00")))
                .extracting("codigo").isEqualTo("conta-nao-ativa");
    }

    @Test
    void transferenciaParaAMesmaContaEhRecusada() {
        Conta conta = contaComSaldo("100.00");

        assertThatThrownBy(() -> Transferencia.entre(conta, conta, Dinheiro.of("10"), "chave-1", AGORA))
                .isInstanceOf(RegraNegocioException.class)
                .extracting("codigo").isEqualTo("mesma-conta");
    }

    @Test
    void transferenciaExigeChaveDeIdempotencia() {
        assertThatThrownBy(() -> Transferencia.entre(contaComSaldo("1"), novaConta(), Dinheiro.of("1"), " ", AGORA))
                .extracting("codigo").isEqualTo("idempotencia-obrigatoria");
    }

    @Test
    void transferenciaRecusaValorNegativo() {
        assertThatThrownBy(() -> Transferencia.entre(contaComSaldo("1"), novaConta(), Dinheiro.of("-1"), "chave", AGORA))
                .extracting("codigo").isEqualTo("valor-invalido");
    }

    private static Movimento movimento(TipoLancamento tipo, String valor) {
        return new Movimento(tipo, Dinheiro.of(valor), "teste", null, 1L, AGORA);
    }

    private static Conta contaComSaldo(String saldo) {
        Conta conta = novaConta();
        conta.creditar(movimento(TipoLancamento.DEPOSITO, saldo));
        return conta;
    }

    private static Conta novaConta() {
        var usuario = Usuario.novo(Cpf.of("52998224725"), "Fulano", null, null, "{noop}x");
        var cliente = Cliente.de(usuario, SegmentoCliente.CORRENTISTA, null);
        return Conta.abrir(new Agencia("0001", "Centro", null), 1, cliente, TipoConta.CORRENTE);
    }

    /** Status só muda por fluxo administrativo ainda não implementado; no teste, direto no campo. */
    private static void definir(Object alvo, String campo, Object valor) throws Exception {
        Field field = alvo.getClass().getDeclaredField(campo);
        field.setAccessible(true);
        field.set(alvo, valor);
    }
}
