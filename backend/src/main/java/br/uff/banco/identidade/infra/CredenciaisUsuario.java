package br.uff.banco.identidade.infra;

import br.uff.banco.identidade.domain.Usuario;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Adaptador de {@link Usuario} para o Spring Security, usado só durante o login. */
record CredenciaisUsuario(Long id, String cpf, String senhaHash, boolean ativo, List<GrantedAuthority> papeis)
        implements UserDetails {

    static CredenciaisUsuario de(Usuario usuario) {
        return new CredenciaisUsuario(
                usuario.getId(),
                usuario.getCpf().numero(),
                usuario.getSenhaHash(),
                usuario.isAtivo(),
                usuario.getPapeis().stream()
                        .<GrantedAuthority>map(p -> new SimpleGrantedAuthority("ROLE_" + p.name()))
                        .toList());
    }

    @Override
    public String getUsername() {
        return cpf;
    }

    @Override
    public String getPassword() {
        return senhaHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return papeis;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }

    @Override
    public String toString() {
        return "CredenciaisUsuario[id=" + id + "]"; // nunca loga CPF nem hash
    }
}
