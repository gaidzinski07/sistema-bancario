package br.uff.banco.suporte;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Atalhos para os testes de integração da API. Credenciais do seed de dev (V1_1__seed_dev.sql). */
public final class ApiDeTeste {

    /** Usuário 4 do seed: só ADMIN. */
    public static final String CPF_ADMIN = "12345678909";
    /** Usuário 3 do seed: só CLIENTE. */
    public static final String CPF_CLIENTE = "39053344705";
    public static final String SENHA_SEED = "Senha@123";

    private ApiDeTeste() {
    }

    public static Tokens login(MockMvc mvc, String cpf, String senha) throws Exception {
        String corpo = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpf": "%s", "senha": "%s"}
                                """.formatted(cpf, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new Tokens(JsonPath.read(corpo, "$.accessToken"), JsonPath.read(corpo, "$.refreshToken"));
    }

    public static Tokens loginAdmin(MockMvc mvc) throws Exception {
        return login(mvc, CPF_ADMIN, SENHA_SEED);
    }

    public static Tokens loginCliente(MockMvc mvc) throws Exception {
        return login(mvc, CPF_CLIENTE, SENHA_SEED);
    }

    public static MockHttpServletRequestBuilder comToken(MockHttpServletRequestBuilder requisicao, String accessToken) {
        return requisicao.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    }

    public record Tokens(String accessToken, String refreshToken) {
    }
}
