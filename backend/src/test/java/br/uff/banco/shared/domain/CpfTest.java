package br.uff.banco.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CpfTest {

    @Test
    void aceitaCpfValidoComOuSemMascara() {
        assertThat(Cpf.of("529.982.247-25").numero()).isEqualTo("52998224725");
        assertThat(Cpf.of("52998224725").numero()).isEqualTo("52998224725");
    }

    @Test
    void formataComMascara() {
        assertThat(Cpf.of("52998224725").formatado()).isEqualTo("529.982.247-25");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "111",            // CPF do seed legado
        "52998224724",    // dígito verificador errado
        "11111111111",    // todos iguais
        "5299822472a",
        "529982247255",
        ""
    })
    void recusaCpfInvalido(String valor) {
        assertThatThrownBy(() -> Cpf.of(valor))
                .isInstanceOf(RegraNegocioException.class)
                .extracting("codigo").isEqualTo("cpf-invalido");
    }

    @Test
    void naoExpoeDocumentoCompletoNoToString() {
        assertThat(Cpf.of("52998224725").toString()).doesNotContain("52998224725").contains("***");
    }
}
