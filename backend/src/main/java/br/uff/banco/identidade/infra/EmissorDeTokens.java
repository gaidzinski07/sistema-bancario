package br.uff.banco.identidade.infra;

import br.uff.banco.identidade.domain.Papel;
import br.uff.banco.identidade.domain.Usuario;
import br.uff.banco.shared.security.JwtProperties;
import br.uff.banco.shared.security.UsuarioAutenticado;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/** Gera access tokens (JWT assinado) e refresh tokens (aleatórios, opacos). */
@Component
public class EmissorDeTokens {

    private static final int BYTES_REFRESH_TOKEN = 32;

    private final JwtEncoder encoder;
    private final JwtProperties propriedades;
    private final SecureRandom aleatorio = new SecureRandom();

    EmissorDeTokens(JwtEncoder encoder, JwtProperties propriedades) {
        this.encoder = encoder;
        this.propriedades = propriedades;
    }

    /**
     * Claims: {@code sub} = ID do usuário, {@code nome}, {@code papeis}. O CPF não vai
     * no token — o payload de um JWT é só Base64, legível por qualquer um.
     */
    public String accessToken(Usuario usuario, Instant agora) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(propriedades.emissor())
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(agora)
                .expiresAt(agora.plus(propriedades.validadeAccessToken()))
                .id(UUID.randomUUID().toString())
                .claim(UsuarioAutenticado.CLAIM_NOME, usuario.getNome())
                .claim(UsuarioAutenticado.CLAIM_PAPEIS, usuario.getPapeis().stream().map(Papel::name).sorted().toList())
                .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
    }

    public String novoRefreshToken() {
        byte[] bytes = new byte[BYTES_REFRESH_TOKEN];
        aleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** O banco guarda só o hash: um vazamento da tabela não entrega tokens utilizáveis. */
    public static String hash(String refreshToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível na JVM", e);
        }
    }

    public long segundosDeValidadeAccessToken() {
        return propriedades.validadeAccessToken().toSeconds();
    }

    public JwtProperties propriedades() {
        return propriedades;
    }
}
