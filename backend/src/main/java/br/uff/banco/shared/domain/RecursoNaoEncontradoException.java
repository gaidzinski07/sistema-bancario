package br.uff.banco.shared.domain;

/**
 * Recurso inexistente <em>ou</em> que não pertence ao usuário autenticado.
 * <p>
 * Os dois casos usam a mesma exceção de propósito: responder 403 para recursos
 * de terceiros revelaria que o ID existe.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public static RecursoNaoEncontradoException de(String recurso, Object id) {
        return new RecursoNaoEncontradoException(recurso + " " + id + " não encontrado(a).");
    }
}
