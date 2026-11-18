package br.uff.banco.identidade;

import static br.uff.banco.suporte.ApiDeTeste.CPF_ADMIN;
import static br.uff.banco.suporte.ApiDeTeste.SENHA_SEED;
import static br.uff.banco.suporte.ApiDeTeste.comToken;
import static br.uff.banco.suporte.ApiDeTeste.login;
import static br.uff.banco.suporte.ApiDeTeste.loginAdmin;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.uff.banco.suporte.ApiDeTeste.Tokens;
import br.uff.banco.suporte.Cpfs;
import br.uff.banco.suporte.TesteDeIntegracao;
import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@TesteDeIntegracao
class AutenticacaoIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    JwtEncoder jwtEncoder;

    @Autowired
    JdbcTemplate jdbc;

    @Nested
    class Login {

        @Test
        void comCpfESenhaCorretosDevolveParDeTokens() throws Exception {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content(corpoLogin("123.456.789-09", SENHA_SEED)))   // CPF com máscara também vale
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tipo").value("Bearer"))
                    .andExpect(jsonPath("$.expiraEmSegundos").value(900))
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @ParameterizedTest(name = "cpf={0}")
        @ValueSource(strings = {"12345678909", "11111111111", "111", "52998224724"})
        void falhasTemSempreAMesmaRespostaGenerica(String cpf) throws Exception {
            // senha errada, CPF inválido e CPF inexistente não podem ser distinguidos
            String senha = cpf.equals(CPF_ADMIN) ? "senha-errada" : SENHA_SEED;
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(corpoLogin(cpf, senha)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.codigo").value("nao-autenticado"))
                    .andExpect(jsonPath("$.detail").value("CPF ou senha inválidos."));
        }

        @Test
        void usuarioInativoNaoLoga() throws Exception {
            String cpf = clienteNovo("SenhaForte1");
            jdbc.update("UPDATE usuario SET ativo = FALSE WHERE cpf = ?", cpf);

            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(corpoLogin(cpf, "SenhaForte1")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("CPF ou senha inválidos."));
        }

        @Test
        void tokenNaoCarregaCpf() throws Exception {
            Tokens tokens = loginAdmin(mvc);
            String payload = new String(java.util.Base64.getUrlDecoder().decode(tokens.accessToken().split("\\.")[1]));

            assertThat(payload).contains("\"sub\":\"4\"", "\"papeis\":[\"ADMIN\"]").doesNotContain(CPF_ADMIN);
        }
    }

    @Nested
    class AccessToken {

        @Test
        void meDevolveOUsuarioDoToken() throws Exception {
            mvc.perform(comToken(get("/api/auth/me"), loginAdmin(mvc).accessToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(4))
                    .andExpect(jsonPath("$.cpf").value("123.456.789-09"))
                    .andExpect(jsonPath("$.perfilAdministrador").value("OPERADOR"));
        }

        @Test
        void semTokenDevolve401ComDesafioBearer() throws Exception {
            mvc.perform(get("/api/auth/me"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                    .andExpect(jsonPath("$.codigo").value("nao-autenticado"));
        }

        @Test
        void tokenAdulteradoEhRecusado() throws Exception {
            String token = loginAdmin(mvc).accessToken();
            mvc.perform(comToken(get("/api/auth/me"), token.substring(0, token.length() - 2) + "xx"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.codigo").value("token-invalido"));
        }

        @Test
        void tokenExpiradoEhRecusado() throws Exception {
            Instant duasHorasAtras = Instant.now().minus(2, ChronoUnit.HOURS);
            mvc.perform(comToken(get("/api/auth/me"), token("banco-backend", duasHorasAtras)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.codigo").value("token-invalido"));
        }

        @Test
        void tokenDeOutroEmissorEhRecusado() throws Exception {
            mvc.perform(comToken(get("/api/auth/me"), token("outro-sistema", Instant.now())))
                    .andExpect(status().isUnauthorized());
        }

        private String token(String emissor, Instant emitidoEm) {
            var claims = JwtClaimsSet.builder()
                    .issuer(emissor).subject("4").issuedAt(emitidoEm).expiresAt(emitidoEm.plus(15, ChronoUnit.MINUTES))
                    .claim("nome", "Teste").claim("papeis", List.of("ADMIN"))
                    .build();
            return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        }
    }

    @Nested
    class RefreshToken {

        @Test
        void rotacionaATokenECadaUmSoVaiUmaVez() throws Exception {
            Tokens login = loginAdmin(mvc);

            Tokens renovado = refresh(login.refreshToken()).andExpect(status().isOk()).andReturnTokens();
            assertThat(renovado.refreshToken()).isNotEqualTo(login.refreshToken());

            // o novo access token funciona
            mvc.perform(comToken(get("/api/auth/me"), renovado.accessToken())).andExpect(status().isOk());
        }

        @Test
        void reusoDeTokenJaUsadoRevogaASessaoInteira() throws Exception {
            Tokens login = loginAdmin(mvc);
            Tokens renovado = refresh(login.refreshToken()).andExpect(status().isOk()).andReturnTokens();

            // alguém reapresenta o token antigo (vazou?)
            refresh(login.refreshToken()).andExpect(status().isUnauthorized());
            // a partir daí, nem o token legítimo mais recente funciona
            refresh(renovado.refreshToken()).andExpect(status().isUnauthorized());
        }

        @Test
        void outrasSessoesNaoSaoAfetadasPeloReuso() throws Exception {
            Tokens sessaoA = loginAdmin(mvc);
            Tokens sessaoB = loginAdmin(mvc);
            refresh(sessaoA.refreshToken()).andExpect(status().isOk());
            refresh(sessaoA.refreshToken()).andExpect(status().isUnauthorized());

            refresh(sessaoB.refreshToken()).andExpect(status().isOk());
        }

        @Test
        void logoutRevogaORefreshToken() throws Exception {
            Tokens login = loginAdmin(mvc);

            mvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON).content(corpoRefresh(login.refreshToken())))
                    .andExpect(status().isNoContent());
            refresh(login.refreshToken()).andExpect(status().isUnauthorized());
        }

        @Test
        void logoutComTokenDesconhecidoNaoEhErro() throws Exception {
            mvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON).content(corpoRefresh("nao-existe")))
                    .andExpect(status().isNoContent());
        }

        @Test
        void refreshTokenExpiradoEhRecusado() throws Exception {
            Tokens login = loginAdmin(mvc);
            jdbc.update("UPDATE refresh_token SET expira_em = ? WHERE token_hash = ?",
                    java.sql.Timestamp.from(Instant.now().minusSeconds(1)),
                    br.uff.banco.identidade.infra.EmissorDeTokens.hash(login.refreshToken()));

            refresh(login.refreshToken()).andExpect(status().isUnauthorized());
        }

        @Test
        void bancoGuardaSoOHashDoRefreshToken() throws Exception {
            Tokens login = loginAdmin(mvc);

            Integer comTokenEmClaro = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM refresh_token WHERE token_hash = ?", Integer.class, login.refreshToken());
            assertThat(comTokenEmClaro).isZero();
        }

        private RefreshResult refresh(String refreshToken) throws Exception {
            return new RefreshResult(mvc.perform(post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON).content(corpoRefresh(refreshToken))));
        }

        private record RefreshResult(ResultActions acoes) {

            RefreshResult andExpect(org.springframework.test.web.servlet.ResultMatcher matcher) throws Exception {
                acoes.andExpect(matcher);
                return this;
            }

            Tokens andReturnTokens() throws Exception {
                String corpo = acoes.andReturn().getResponse().getContentAsString();
                return new Tokens(JsonPath.read(corpo, "$.accessToken"), JsonPath.read(corpo, "$.refreshToken"));
            }
        }
    }

    /** Cadastra um cliente novo pela API de admin e devolve o CPF. */
    private String clienteNovo(String senha) throws Exception {
        String cpf = Cpfs.novo();
        mvc.perform(comToken(post("/api/admin/clientes"), loginAdmin(mvc).accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Cliente Teste", "cpf": "%s", "senha": "%s", "segmento": "CORRENTISTA"}
                                """.formatted(cpf, senha)))
                .andExpect(status().isCreated());
        // e consegue logar antes de ser desativado
        login(mvc, cpf, senha);
        return cpf;
    }

    private static String corpoLogin(String cpf, String senha) {
        return """
                {"cpf": "%s", "senha": "%s"}
                """.formatted(cpf, senha);
    }

    private static String corpoRefresh(String refreshToken) {
        return """
                {"refreshToken": "%s"}
                """.formatted(refreshToken);
    }
}
