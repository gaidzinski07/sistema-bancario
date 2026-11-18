package br.uff.banco.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DinheiroTest {

    @Test
    void normalizaParaDuasCasasComArredondamentoBancario() {
        assertThat(Dinheiro.of("10").valor()).isEqualByComparingTo("10.00");
        assertThat(Dinheiro.of("10.125").valor()).isEqualByComparingTo("10.12"); // HALF_EVEN
        assertThat(Dinheiro.of("10.135").valor()).isEqualByComparingTo("10.14");
        assertThat(Dinheiro.of("10").valor().scale()).isEqualTo(2);
    }

    @Test
    void naoAcumulaErroDeArredondamentoComoFloat() {
        // Com float (como no legado), 0.1 somado 10 vezes dá 1.0000001.
        var total = Dinheiro.ZERO;
        for (int i = 0; i < 10; i++) {
            total = total.somar(Dinheiro.of("0.10"));
        }
        assertThat(total).isEqualTo(Dinheiro.of("1.00"));
    }

    @Test
    void igualdadeIndependeDaEscalaDeEntrada() {
        assertThat(Dinheiro.of("5")).isEqualTo(Dinheiro.of(new BigDecimal("5.000")));
    }

    @Test
    void operacoesEComparacoes() {
        var cem = Dinheiro.of("100");
        var trinta = Dinheiro.of("30");

        assertThat(cem.subtrair(trinta)).isEqualTo(Dinheiro.of("70"));
        assertThat(trinta.subtrair(cem).isNegativo()).isTrue();
        assertThat(trinta.isMenorQue(cem)).isTrue();
        assertThat(cem.multiplicar(new BigDecimal("1.5"))).isEqualTo(Dinheiro.of("150"));
    }

    @Test
    void exigirPositivoRecusaZeroENegativo() {
        assertThat(Dinheiro.of("0.01").exigirPositivo()).isEqualTo(Dinheiro.of("0.01"));
        assertThatThrownBy(() -> Dinheiro.ZERO.exigirPositivo()).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> Dinheiro.of("-5").exigirPositivo())
                .isInstanceOf(RegraNegocioException.class)
                .extracting("codigo").isEqualTo("valor-invalido");
    }
}
