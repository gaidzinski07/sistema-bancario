package br.uff.banco.identidade.application;

/**
 * Resposta de login/refresh.
 *
 * @param accessToken      JWT a enviar em {@code Authorization: Bearer ...}
 * @param tipo             sempre {@code Bearer}
 * @param expiraEmSegundos validade do access token
 * @param refreshToken     token opaco para obter um novo par em {@code /api/auth/refresh} (uso único)
 */
public record TokensView(String accessToken, String tipo, long expiraEmSegundos, String refreshToken) {

    static TokensView bearer(String accessToken, long expiraEmSegundos, String refreshToken) {
        return new TokensView(accessToken, "Bearer", expiraEmSegundos, refreshToken);
    }
}
