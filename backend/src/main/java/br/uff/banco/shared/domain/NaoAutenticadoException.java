package br.uff.banco.shared.domain;

/**
 * Credenciais ausentes, inválidas ou expiradas (login, refresh token).
 * <p>
 * A mensagem é sempre genérica: não revela se o CPF existe, se a senha está
 * errada ou se o token foi revogado.
 */
public class NaoAutenticadoException extends RuntimeException {

    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }
}
