package br.uff.banco.identidade.api;

import br.uff.banco.identidade.application.AutenticacaoService;
import br.uff.banco.identidade.application.SenhaTransacaoService;
import br.uff.banco.identidade.application.TokensView;
import br.uff.banco.identidade.application.UsuarioView;
import br.uff.banco.shared.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação")
@RestController
@RequestMapping("/api/auth")
class AuthController {

    private final AutenticacaoService autenticacao;
    private final SenhaTransacaoService senhaTransacao;

    AuthController(AutenticacaoService autenticacao, SenhaTransacaoService senhaTransacao) {
        this.autenticacao = autenticacao;
        this.senhaTransacao = senhaTransacao;
    }

    @Operation(summary = "Login com CPF e senha", description = "Vale para clientes e administradores. Retorna o access token (JWT) e o refresh token.")
    @PostMapping("/login")
    TokensView login(@Valid @RequestBody LoginRequest requisicao) {
        return autenticacao.login(requisicao.cpf(), requisicao.senha());
    }

    @Operation(summary = "Renova o access token", description = "O refresh token é de uso único: a resposta traz um novo.")
    @PostMapping("/refresh")
    TokensView refresh(@Valid @RequestBody RefreshRequest requisicao) {
        return autenticacao.renovar(requisicao.refreshToken());
    }

    @Operation(summary = "Logout", description = "Revoga o refresh token. O access token continua válido até expirar (até 15 min) e deve ser descartado pelo cliente.")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(@Valid @RequestBody RefreshRequest requisicao) {
        autenticacao.logout(requisicao.refreshToken());
    }

    @Operation(summary = "Dados do usuário autenticado")
    @GetMapping("/me")
    UsuarioView me(UsuarioAutenticado usuario) {
        return autenticacao.usuarioAtual(usuario.id());
    }

    @Operation(summary = "Define ou troca a senha de transação", description = "4 a 6 dígitos. Exige a senha de login.")
    @PutMapping("/senha-transacao")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void definirSenhaTransacao(UsuarioAutenticado usuario, @Valid @RequestBody SenhaTransacaoRequest requisicao) {
        senhaTransacao.definir(usuario.id(), requisicao.senhaLogin(), requisicao.novaSenhaTransacao());
    }

    record SenhaTransacaoRequest(
            @NotBlank @Size(max = 72) String senhaLogin,
            @NotBlank @Pattern(regexp = "\\d{4,6}", message = "deve ter de 4 a 6 dígitos") String novaSenhaTransacao) {

        @Override
        public String toString() {
            return "SenhaTransacaoRequest[***]";
        }
    }

    record LoginRequest(@NotBlank @Size(max = 14) String cpf, @NotBlank @Size(max = 72) String senha) {

        @Override
        public String toString() {
            return "LoginRequest[***]";
        }
    }

    record RefreshRequest(@NotBlank @Size(max = 100) String refreshToken) {

        @Override
        public String toString() {
            return "RefreshRequest[***]";
        }
    }
}
