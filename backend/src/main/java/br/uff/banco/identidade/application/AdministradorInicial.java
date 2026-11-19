package br.uff.banco.identidade.application;

import br.uff.banco.identidade.domain.Papel;
import br.uff.banco.identidade.domain.PerfilAdministrador;
import br.uff.banco.identidade.infra.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cria o primeiro administrador de um ambiente novo.
 * <p>
 * Em produção não há seed: sem isso, ninguém conseguiria logar para cadastrar o resto. Lê
 * {@code banco.admin-inicial.*} (variáveis {@code BANCO_ADMIN_INICIAL_CPF}, {@code _NOME} e
 * {@code _SENHA}) e só age se ainda não existir nenhum administrador — reiniciar a aplicação com as
 * variáveis definidas não cria outro nem altera a senha. Depois do primeiro uso, as variáveis podem
 * (e devem) ser removidas do ambiente.
 */
@Component
@EnableConfigurationProperties(AdministradorInicial.Propriedades.class)
class AdministradorInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdministradorInicial.class);

    private final Propriedades propriedades;
    private final UsuarioRepository usuarios;
    private final CadastroUsuarioService cadastro;

    AdministradorInicial(Propriedades propriedades, UsuarioRepository usuarios, CadastroUsuarioService cadastro) {
        this.propriedades = propriedades;
        this.usuarios = usuarios;
        this.cadastro = cadastro;
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        if (!propriedades.configurado()) {
            return;
        }
        if (usuarios.existeComPapel(Papel.ADMIN)) {
            log.info("Administrador inicial configurado, mas já existe administrador: nada a fazer.");
            return;
        }
        UsuarioView admin = cadastro.criarAdministrador(
                new NovoUsuario(propriedades.nome(), propriedades.cpf(), null, null, propriedades.senha()),
                PerfilAdministrador.GERENTE);
        log.warn("Administrador inicial criado (usuário {}). Remova as variáveis BANCO_ADMIN_INICIAL_* do ambiente.", admin.id());
    }

    @ConfigurationProperties("banco.admin-inicial")
    record Propriedades(String cpf, String nome, String senha) {

        Propriedades {
            nome = nome == null || nome.isBlank() ? "Administrador" : nome;
        }

        boolean configurado() {
            boolean cpfInformado = cpf != null && !cpf.isBlank();
            if (cpfInformado && (senha == null || senha.length() < 8)) {
                throw new IllegalStateException("BANCO_ADMIN_INICIAL_SENHA é obrigatória e precisa ter pelo menos 8 caracteres.");
            }
            return cpfInformado;
        }

        @Override
        public String toString() {
            return "Propriedades[nome=" + nome + "]";
        }
    }
}
