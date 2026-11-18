package br.uff.banco.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class JwtPropertiesTest {

    private static final String SEGREDO_256_BITS = Base64.getEncoder().encodeToString(new byte[32]);

    @Test
    void aplicaDefaults() {
        var propriedades = new JwtProperties(SEGREDO_256_BITS, null, null, null);

        assertThat(propriedades.emissor()).isEqualTo("banco-backend");
        assertThat(propriedades.validadeAccessToken()).isEqualTo(Duration.ofMinutes(15));
        assertThat(propriedades.validadeRefreshToken()).isEqualTo(Duration.ofDays(7));
        assertThat(propriedades.chave().getAlgorithm()).isEqualTo("HmacSHA256");
    }

    @Test
    void exigeSegredo() {
        assertThatThrownBy(() -> new JwtProperties("", null, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    void recusaSegredoMenorQue256Bits() {
        String curto = Base64.getEncoder().encodeToString(new byte[31]);

        assertThatThrownBy(() -> new JwtProperties(curto, null, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("256 bits");
    }
}
