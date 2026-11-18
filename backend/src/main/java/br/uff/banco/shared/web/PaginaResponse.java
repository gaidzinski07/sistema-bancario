package br.uff.banco.shared.web;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/**
 * Formato estável de resposta paginada da API.
 * <p>
 * Evita serializar o {@link Page} do Spring Data direto, cujo JSON é detalhe de
 * implementação e muda entre versões.
 */
public record PaginaResponse<T>(List<T> conteudo, int pagina, int tamanho, long totalElementos, int totalPaginas) {

    public static <E, T> PaginaResponse<T> de(Page<E> pagina, Function<E, T> conversor) {
        return new PaginaResponse<>(
                pagina.getContent().stream().map(conversor).toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages());
    }

    public static <T> PaginaResponse<T> de(Page<T> pagina) {
        return de(pagina, Function.identity());
    }
}
