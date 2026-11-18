package br.uff.banco.identidade.infra;

import br.uff.banco.shared.domain.Cpf;
import br.uff.banco.shared.domain.RegraNegocioException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carrega o usuário pelo CPF para o login.
 * <p>
 * CPF malformado e CPF inexistente dão o mesmo resultado ({@code UsernameNotFoundException},
 * que o Spring transforma em "credenciais inválidas"), sem revelar qual dos dois ocorreu.
 */
@Service
class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    UsuarioDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String cpfInformado) {
        Cpf cpf;
        try {
            cpf = Cpf.of(cpfInformado);
        } catch (RegraNegocioException | NullPointerException e) {
            throw new UsernameNotFoundException("CPF inválido");
        }
        return usuarios.findByCpf(cpf)
                .map(CredenciaisUsuario::de)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
