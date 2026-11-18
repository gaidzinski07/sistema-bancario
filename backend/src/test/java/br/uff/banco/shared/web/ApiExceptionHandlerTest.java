package br.uff.banco.shared.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.uff.banco.shared.domain.RecursoNaoEncontradoException;
import br.uff.banco.shared.domain.RegraNegocioException;
import br.uff.banco.shared.security.SecurityConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = ApiExceptionHandlerTest.ControllerDeTeste.class)
@Import({SecurityConfig.class, ApiExceptionHandlerTest.ControllerDeTeste.class})
@WithMockUser
class ApiExceptionHandlerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void regraDeNegocioVira422ComCodigo() throws Exception {
        mvc.perform(get("/teste/regra"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.codigo").value("saldo-insuficiente"))
                .andExpect(jsonPath("$.detail").value("Saldo insuficiente."));
    }

    @Test
    void recursoNaoEncontradoVira404() throws Exception {
        mvc.perform(get("/teste/nao-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("nao-encontrado"));
    }

    @Test
    void erroInesperadoVira500SemVazarDetalhes() throws Exception {
        mvc.perform(get("/teste/erro"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("erro-interno"))
                .andExpect(jsonPath("$.detail").value("Ocorreu um erro inesperado. Tente novamente mais tarde."));
    }

    @Test
    void validacaoVira400ComErrosPorCampo() throws Exception {
        mvc.perform(post("/teste/validacao").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\": \"\", \"valor\": -1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("dados-invalidos"))
                .andExpect(jsonPath("$.erros.length()").value(2));
    }

    @Test
    void postSemTokenCsrfEhRecusado() throws Exception {
        mvc.perform(post("/teste/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\": \"x\", \"valor\": 1}"))
                .andExpect(status().isForbidden());
    }

    @RestController
    static class ControllerDeTeste {

        @GetMapping("/teste/regra")
        void regra() {
            throw new RegraNegocioException("saldo-insuficiente", "Saldo insuficiente.");
        }

        @GetMapping("/teste/nao-encontrado")
        void naoEncontrado() {
            throw RecursoNaoEncontradoException.de("Conta", 99);
        }

        @GetMapping("/teste/erro")
        void erro() {
            throw new IllegalStateException("detalhe interno que não pode vazar");
        }

        @PostMapping("/teste/validacao")
        void validacao(@Valid @RequestBody Requisicao requisicao) {
        }

        record Requisicao(@NotBlank String descricao, @Positive BigDecimal valor) {
        }
    }
}
