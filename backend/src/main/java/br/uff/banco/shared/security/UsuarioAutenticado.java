package br.uff.banco.shared.security;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Quem está chamando a API, extraído do JWT validado.
 * <p>
 * Controllers recebem este objeto como parâmetro (ver {@link UsuarioAutenticadoArgumentResolver})
 * em vez de aceitar o ID do usuário no corpo/URL — no legado, deposito/saque/
 * transferência usavam {@code id_logado = 1} fixo.
 */
public record UsuarioAutenticado(Long id, String nome, Set<String> papeis) {

    public static final String CLAIM_NOME = "nome";
    public static final String CLAIM_PAPEIS = "papeis";

    public static UsuarioAutenticado de(Jwt jwt) {
        List<String> papeis = jwt.getClaimAsStringList(CLAIM_PAPEIS);
        return new UsuarioAutenticado(
                Long.valueOf(jwt.getSubject()),
                jwt.getClaimAsString(CLAIM_NOME),
                papeis == null ? Set.of() : papeis.stream().collect(Collectors.toUnmodifiableSet()));
    }

    public boolean possuiPapel(String papel) {
        return papeis.contains(papel);
    }
}
