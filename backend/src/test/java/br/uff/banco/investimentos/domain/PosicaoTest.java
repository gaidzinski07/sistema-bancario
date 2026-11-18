package br.uff.banco.investimentos.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.uff.banco.agencias.domain.Agencia;
import br.uff.banco.clientes.domain.Cliente;
import br.uff.banco.clientes.domain.SegmentoCliente;
import br.uff.banco.contas.domain.Conta;
import br.uff.banco.contas.domain.TipoConta;
import br.uff.banco.identidade.domain.Usuario;
import br.uff.banco.shared.domain.Cpf;
import br.uff.banco.shared.domain.Dinheiro;
import br.uff.banco.shared.domain.RegraNegocioException;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PosicaoTest {

    private static final Instant AGORA = Instant.parse("2026-11-18T12:00:00Z");

    @Test
    void aplicacaoCompraCotasPelaCotaAtual() {
        Posicao posicao = posicao(fundo("10", "100"));

        MovimentoInvestimento movimento = posicao.aplicar(Dinheiro.of("1000"), AGORA);

        assertThat(movimento.getTipo()).isEqualTo(TipoMovimentoInvestimento.APLICACAO);
        assertThat(movimento.getQuantidadeCotas()).isEqualByComparingTo("100");
        assertThat(movimento.getValorCota()).isEqualByComparingTo("10");
        assertThat(posicao.getQuantidadeCotas()).isEqualByComparingTo("100");
        assertThat(posicao.getValorAplicado()).isEqualTo(Dinheiro.of("1000"));
        assertThat(posicao.valorAtual()).isEqualTo(Dinheiro.of("1000"));
    }

    /** Legado: rendimento era regra de três sobre o valor investido, ignorando as cotas. */
    @Test
    void rendimentoSaiDasCotasVezesACotaAtual() {
        Fundo fundo = fundo("10", "100");
        Posicao posicao = posicao(fundo);
        posicao.aplicar(Dinheiro.of("1000"), AGORA);

        fundo.atualizarCota(new BigDecimal("10.5"));

        assertThat(posicao.valorAtual()).isEqualTo(Dinheiro.of("1050"));
        assertThat(posicao.rendimento()).isEqualTo(Dinheiro.of("50"));

        fundo.atualizarCota(new BigDecimal("9"));
        assertThat(posicao.rendimento()).isEqualTo(Dinheiro.of("-100"));
    }

    @Test
    void cotasDaAplicacaoSaoTruncadasEOValorAtualTambem() {
        Posicao posicao = posicao(fundo("3", "1"));

        posicao.aplicar(Dinheiro.of("100"), AGORA);

        assertThat(posicao.getQuantidadeCotas()).isEqualByComparingTo("33.33333333");  // 33,333... truncado
        assertThat(posicao.valorAtual()).isEqualTo(Dinheiro.of("99.99"));            // 99,99999999 truncado
    }

    @Test
    void resgateParcialBaixaCotasECustoNaProporcao() {
        Fundo fundo = fundo("10", "100");
        Posicao posicao = posicao(fundo);
        posicao.aplicar(Dinheiro.of("1000"), AGORA);
        fundo.atualizarCota(new BigDecimal("12.5"));                                  // vale 1250

        MovimentoInvestimento resgate = posicao.resgatar(Dinheiro.of("500"), AGORA);

        assertThat(resgate.getTipo()).isEqualTo(TipoMovimentoInvestimento.RESGATE);
        assertThat(resgate.getQuantidadeCotas()).isEqualByComparingTo("40");
        assertThat(resgate.getValor()).isEqualTo(Dinheiro.of("500"));
        assertThat(posicao.getQuantidadeCotas()).isEqualByComparingTo("60");
        assertThat(posicao.getValorAplicado()).isEqualTo(Dinheiro.of("600"));         // 1000 × 60/100
        assertThat(posicao.valorAtual()).isEqualTo(Dinheiro.of("750"));
        assertThat(posicao.rendimento()).isEqualTo(Dinheiro.of("150"));                // os mesmos 25%
    }

    @Test
    void cotasDoResgateParcialSaoArredondadasParaCimaECobremOValorPago() {
        Posicao posicao = posicao(fundo("3", "1"));
        posicao.aplicar(Dinheiro.of("100"), AGORA);

        MovimentoInvestimento resgate = posicao.resgatar(Dinheiro.of("10"), AGORA);

        assertThat(resgate.getQuantidadeCotas()).isEqualByComparingTo("3.33333334");
        assertThat(resgate.getQuantidadeCotas().multiply(resgate.getValorCota())).isGreaterThanOrEqualTo(new BigDecimal("10"));
    }

    @Test
    void resgateTotalPagaOValorAtualEZeraAPosicao() {
        Fundo fundo = fundo("10", "100");
        Posicao posicao = posicao(fundo);
        posicao.aplicar(Dinheiro.of("1000"), AGORA);
        fundo.atualizarCota(new BigDecimal("11"));

        MovimentoInvestimento resgate = posicao.resgatarTudo(AGORA);

        assertThat(resgate.getValor()).isEqualTo(Dinheiro.of("1100"));
        assertThat(posicao.isZerada()).isTrue();
        assertThat(posicao.getValorAplicado()).isEqualTo(Dinheiro.ZERO);
    }

    @Test
    void resgateDoValorTotalViaParcialTambemZera() {
        Posicao posicao = posicao(fundo("10", "100"));
        posicao.aplicar(Dinheiro.of("1000"), AGORA);

        posicao.resgatar(Dinheiro.of("1000"), AGORA);

        assertThat(posicao.isZerada()).isTrue();
        assertThat(posicao.getValorAplicado()).isEqualTo(Dinheiro.ZERO);
    }

    @Test
    void naoResgataMaisDoQueAPosicaoVale() {
        Posicao posicao = posicao(fundo("10", "100"));
        posicao.aplicar(Dinheiro.of("1000"), AGORA);

        assertThatThrownBy(() -> posicao.resgatar(Dinheiro.of("1000.01"), AGORA))
                .isInstanceOf(RegraNegocioException.class)
                .extracting("codigo").isEqualTo("saldo-investido-insuficiente");
        assertThat(posicao.getQuantidadeCotas()).isEqualByComparingTo("100");
    }

    @Test
    void posicaoZeradaNaoTemOQueResgatar() {
        Posicao posicao = posicao(fundo("10", "100"));

        assertThatThrownBy(() -> posicao.resgatarTudo(AGORA)).extracting("codigo").isEqualTo("posicao-vazia");
    }

    @Test
    void aplicacaoAbaixoDoMinimoEhRecusada() {
        Posicao posicao = posicao(fundo("10", "500"));

        assertThatThrownBy(() -> posicao.aplicar(Dinheiro.of("499.99"), AGORA))
                .extracting("codigo").isEqualTo("abaixo-da-aplicacao-minima");
    }

    @Test
    void fundoInativoRecusaAplicacaoMasPermiteResgate() {
        Fundo fundo = fundo("10", "100");
        Posicao posicao = posicao(fundo);
        posicao.aplicar(Dinheiro.of("1000"), AGORA);
        fundo.desativar();

        assertThatThrownBy(() -> posicao.aplicar(Dinheiro.of("100"), AGORA)).extracting("codigo").isEqualTo("fundo-inativo");
        assertThat(posicao.resgatarTudo(AGORA).getValor()).isEqualTo(Dinheiro.of("1000"));
    }

    @Test
    void soAbrePosicaoAPartirDeContaCorrente() {
        assertThatThrownBy(() -> Posicao.abrir(conta(TipoConta.POUPANCA), fundo("10", "100")))
                .extracting("codigo").isEqualTo("conta-nao-corrente");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "1.123456789"})
    void cotaInvalidaEhRecusada(String cota) {
        assertThatThrownBy(() -> fundo("10", "100").atualizarCota(new BigDecimal(cota)))
                .extracting("codigo").isEqualTo("cota-invalida");
    }

    private static Fundo fundo(String cota, String minimo) {
        return new Fundo("Fundo Teste", new BigDecimal(cota), Dinheiro.of(minimo));
    }

    private static Posicao posicao(Fundo fundo) {
        return Posicao.abrir(conta(TipoConta.CORRENTE), fundo);
    }

    private static Conta conta(TipoConta tipo) {
        var usuario = Usuario.novo(Cpf.of("52998224725"), "Fulano", null, null, "{noop}x");
        var cliente = Cliente.de(usuario, SegmentoCliente.INVESTIDOR, null);
        return Conta.abrir(new Agencia("0001", "Centro", null), 1, cliente, tipo);
    }
}
