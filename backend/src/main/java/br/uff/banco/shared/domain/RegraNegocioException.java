package br.uff.banco.shared.domain;

/**
 * Violação de regra de negócio (saldo insuficiente, valor inválido, etc.).
 * <p>
 * O {@code codigo} é estável e vai para o cliente da API no {@code ProblemDetail},
 * permitindo que o front trate casos específicos sem depender do texto da mensagem.
 * Módulos podem estender esta classe para erros próprios.
 */
public class RegraNegocioException extends RuntimeException {

    private final String codigo;

    public RegraNegocioException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
