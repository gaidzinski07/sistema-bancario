package br.uff.banco.identidade.api;

import br.uff.banco.identidade.application.CadastroUsuarioService;
import br.uff.banco.identidade.application.NovoUsuario;
import br.uff.banco.identidade.application.UsuarioView;
import br.uff.banco.identidade.domain.PerfilAdministrador;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin — administradores")
@RestController
@RequestMapping("/api/admin/administradores")
class AdminAdministradorController {

    private final CadastroUsuarioService cadastro;

    AdminAdministradorController(CadastroUsuarioService cadastro) {
        this.cadastro = cadastro;
    }

    @Operation(summary = "Cadastra um administrador")
    @PostMapping
    ResponseEntity<UsuarioView> criar(@Valid @RequestBody NovoAdministradorRequest requisicao) {
        UsuarioView criado = cadastro.criarAdministrador(
                new NovoUsuario(requisicao.nome(), requisicao.cpf(), requisicao.email(), requisicao.dataNascimento(), requisicao.senha()),
                requisicao.perfil());
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    record NovoAdministradorRequest(
            @NotBlank @Size(max = 100) String nome,
            @NotBlank @Size(max = 14) String cpf,
            @Email @Size(max = 150) String email,
            @Past LocalDate dataNascimento,
            @NotBlank @Size(min = 8, max = 72) String senha,
            @NotNull PerfilAdministrador perfil) {

        @Override
        public String toString() {
            return "NovoAdministradorRequest[nome=" + nome + "]";
        }
    }
}
