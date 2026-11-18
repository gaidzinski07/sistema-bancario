package br.uff.banco.shared.web;

import br.uff.banco.shared.domain.NaoAutenticadoException;
import br.uff.banco.shared.domain.RecursoNaoEncontradoException;
import br.uff.banco.shared.domain.RegraNegocioException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduz exceções em respostas {@link ProblemDetail} (RFC 9457).
 * <p>
 * Toda resposta de erro tem a propriedade {@code codigo}, estável, para o front
 * tratar casos específicos. Erros inesperados nunca expõem detalhes internos —
 * no legado, falhas eram engolidas com {@code System.err.println} e a tela
 * seguia como se nada tivesse acontecido.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(RegraNegocioException.class)
    ProblemDetail regraNegocio(RegraNegocioException ex) {
        return problema(HttpStatus.UNPROCESSABLE_CONTENT, ex.getCodigo(), ex.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ProblemDetail naoEncontrado(RecursoNaoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "nao-encontrado", ex.getMessage());
    }

    @ExceptionHandler(NaoAutenticadoException.class)
    ResponseEntity<ProblemDetail> naoAutenticado(NaoAutenticadoException ex) {
        return naoAutenticado("nao-autenticado", ex.getMessage());
    }

    /** Vem do filtro de segurança (token ausente, inválido ou expirado), via entry point. */
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ProblemDetail> falhaDeAutenticacao(AuthenticationException ex) {
        if (ex instanceof InvalidBearerTokenException) {
            return naoAutenticado("token-invalido", "Token inválido ou expirado.");
        }
        return naoAutenticado("nao-autenticado", "Autenticação necessária.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail acessoNegado(AccessDeniedException ex) {
        return problema(HttpStatus.FORBIDDEN, "acesso-negado", "Você não tem permissão para esta operação.");
    }

    /** Corrida entre a checagem de unicidade (CPF, e-mail, código) e o INSERT. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail violacaoDeIntegridade(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade: {}", ex.getMostSpecificCause().getMessage());
        return problema(HttpStatus.CONFLICT, "conflito", "O registro conflita com dados já existentes.");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail violacaoDeRestricao(ConstraintViolationException ex) {
        var problema = problema(HttpStatus.BAD_REQUEST, "dados-invalidos", "Dados inválidos.");
        problema.setProperty("erros", ex.getConstraintViolations().stream()
                .map(v -> new ErroDeCampo(v.getPropertyPath().toString(), v.getMessage()))
                .toList());
        return problema;
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail conflitoDeConcorrencia(ObjectOptimisticLockingFailureException ex) {
        return problema(HttpStatus.CONFLICT, "conflito-concorrencia",
                "O registro foi alterado por outra operação. Tente novamente.");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail erroInesperado(Exception ex) {
        log.error("Erro não tratado", ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "erro-interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var problema = problema(HttpStatus.BAD_REQUEST, "dados-invalidos", "Dados inválidos.");
        List<ErroDeCampo> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ErroDeCampo(e.getField(), e.getDefaultMessage()))
                .toList();
        problema.setProperty("erros", erros);
        return ResponseEntity.badRequest().headers(headers).body(problema);
    }

    /** JSON malformado, tipo errado (ex.: texto em campo numérico) ou valor de enum inexistente. */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest().headers(headers).body(
                problema(HttpStatus.BAD_REQUEST, "corpo-invalido", "O corpo da requisição é inválido ou está malformado."));
    }

    /** Header obrigatório ausente (ex.: {@code Idempotency-Key}). */
    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String detalhe = ex instanceof MissingRequestHeaderException faltando
                ? "O header " + faltando.getHeaderName() + " é obrigatório."
                : "Requisição inválida.";
        return ResponseEntity.badRequest().headers(headers).body(problema(HttpStatus.BAD_REQUEST, "requisicao-invalida", detalhe));
    }

    /** Validação de parâmetros de URL/query (ex.: {@code @Max} em {@code tamanho}). */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var problema = problema(HttpStatus.BAD_REQUEST, "dados-invalidos", "Dados inválidos.");
        problema.setProperty("erros", ex.getParameterValidationResults().stream()
                .flatMap(resultado -> resultado.getResolvableErrors().stream()
                        .map(erro -> new ErroDeCampo(resultado.getMethodParameter().getParameterName(), erro.getDefaultMessage())))
                .toList());
        return ResponseEntity.badRequest().headers(headers).body(problema);
    }

    private static ResponseEntity<ProblemDetail> naoAutenticado(String codigo, String detalhe) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .body(problema(HttpStatus.UNAUTHORIZED, codigo, detalhe));
    }

    private static ProblemDetail problema(HttpStatus status, String codigo, String detalhe) {
        var problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setProperty("codigo", codigo);
        return problema;
    }

    public record ErroDeCampo(String campo, String mensagem) {
    }
}
