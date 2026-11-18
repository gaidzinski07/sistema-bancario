package br.uff.banco.shared.security;

import java.time.Duration;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração dos tokens ({@code banco.seguranca.jwt.*}).
 *
 * @param segredo              chave HMAC em Base64, com pelo menos 256 bits (32 bytes). Em produção vem de {@code JWT_SECRET}.
 * @param emissor              valor do claim {@code iss}, conferido na validação
 * @param validadeAccessToken  vida do JWT; curta, porque um access token não pode ser revogado
 * @param validadeRefreshToken vida do refresh token (este sim revogável, no banco)
 */
@ConfigurationProperties("banco.seguranca.jwt")
public record JwtProperties(String segredo, String emissor, Duration validadeAccessToken, Duration validadeRefreshToken) {

    private static final int TAMANHO_MINIMO_BYTES = 32;

    public JwtProperties {
        if (segredo == null || segredo.isBlank()) {
            throw new IllegalStateException("banco.seguranca.jwt.segredo (JWT_SECRET) não foi configurado.");
        }
        if (Base64.getDecoder().decode(segredo).length < TAMANHO_MINIMO_BYTES) {
            throw new IllegalStateException("banco.seguranca.jwt.segredo precisa ter pelo menos 256 bits (32 bytes em Base64).");
        }
        if (emissor == null || emissor.isBlank()) {
            emissor = "banco-backend";
        }
        if (validadeAccessToken == null) {
            validadeAccessToken = Duration.ofMinutes(15);
        }
        if (validadeRefreshToken == null) {
            validadeRefreshToken = Duration.ofDays(7);
        }
    }

    public SecretKey chave() {
        return new SecretKeySpec(Base64.getDecoder().decode(segredo), "HmacSHA256");
    }
}
