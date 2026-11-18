package br.uff.banco.agencias.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.uff.banco.shared.domain.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class AgenciaTest {

    @Test
    void reservaNumerosSequenciais() {
        var agencia = new Agencia("0001", "Centro", null);

        assertThat(agencia.reservarNumeroConta()).isEqualTo(1);
        assertThat(agencia.reservarNumeroConta()).isEqualTo(2);
        assertThat(agencia.reservarNumeroConta()).isEqualTo(3);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"1", "00001", "abcd", "12a4"})
    void recusaCodigoQueNaoTemQuatroDigitos(String codigo) {
        assertThatThrownBy(() -> new Agencia(codigo, "Centro", null))
                .isInstanceOf(RegraNegocioException.class)
                .extracting("codigo").isEqualTo("codigo-agencia-invalido");
    }
}
