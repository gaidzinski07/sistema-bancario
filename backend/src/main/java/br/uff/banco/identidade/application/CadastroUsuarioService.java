package br.uff.banco.identidade.application;

import br.uff.banco.identidade.domain.PerfilAdministrador;
import br.uff.banco.identidade.domain.Usuario;
import br.uff.banco.identidade.infra.UsuarioRepository;
import br.uff.banco.shared.domain.Cpf;
import br.uff.banco.shared.domain.RegraNegocioException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Criação de usuários. Usado pelo cadastro de clientes e de administradores. */
@Service
public class CadastroUsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    CadastroUsuarioService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cria e persiste um usuário ainda sem papéis. Deve rodar dentro da transação de
     * quem chama (cadastro de cliente/admin), para que tudo seja gravado junto.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Usuario criar(NovoUsuario dados) {
        Cpf cpf = Cpf.of(dados.cpf());
        if (usuarios.existsByCpf(cpf)) {
            throw new RegraNegocioException("cpf-ja-cadastrado", "Já existe um usuário com este CPF.");
        }
        if (dados.email() != null && !dados.email().isBlank() && usuarios.existsByEmail(dados.email().trim().toLowerCase())) {
            throw new RegraNegocioException("email-ja-cadastrado", "Já existe um usuário com este e-mail.");
        }
        Usuario usuario = Usuario.novo(cpf, dados.nome(), dados.email(), dados.dataNascimento(),
                passwordEncoder.encode(dados.senha()));
        return usuarios.save(usuario);
    }

    @Transactional
    public UsuarioView criarAdministrador(NovoUsuario dados, PerfilAdministrador perfil) {
        Usuario usuario = criar(dados);
        usuario.tornarAdministrador(perfil);
        return UsuarioView.de(usuario);
    }
}
