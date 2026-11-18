package br.uff.banco.shared.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.uff.banco.shared.security.SecurityConfig;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestController;

/**
 * Frontend servido pela API passando pela pilha MVC + segurança de verdade (anônimo).
 * O teste do resolvedor sozinho não pegava que a raiz `/` nunca chega até ele.
 */
@WebMvcTest(controllers = FrontendRotasTest.SemControllers.class)
@Import({SecurityConfig.class, FrontendConfig.class})
class FrontendRotasTest {

    private static final Path BUILD = criarBuild();

    @Autowired
    MockMvc mvc;

    @MockitoBean
    UserDetailsService usuarios;

    @DynamicPropertySource
    static void diretorioDoFrontend(DynamicPropertyRegistry registro) {
        registro.add("banco.frontend.diretorio", BUILD::toString);
    }

    @Test
    void raizEncaminhaParaOIndex() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/index.html", "/inicio", "/contas/5/extrato"})
    void rotasDoFrontSaoPublicasEDevolvemOIndex(String rota) throws Exception {
        mvc.perform(get(rota)).andExpect(status().isOk()).andExpect(content().string("<app-root></app-root>"));
    }

    @Test
    void arquivosEstaticosSaoServidos() throws Exception {
        mvc.perform(get("/main-ABC123.js")).andExpect(status().isOk()).andExpect(content().string("console.log('app')"));
    }

    @Test
    void arquivoInexistenteEh404() throws Exception {
        mvc.perform(get("/nao-existe.js")).andExpect(status().isNotFound());
    }

    @Test
    void apiContinuaExigindoAutenticacao() throws Exception {
        mvc.perform(get("/api/contas")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/qualquer-coisa")).andExpect(status().isUnauthorized());
    }

    /** Restringe a fatia MVC: o teste só exercita recursos estáticos e segurança. */
    @RestController
    static class SemControllers {
    }

    private static Path criarBuild() {
        try {
            Path pasta = Files.createTempDirectory("frontend-build");
            Files.writeString(pasta.resolve("index.html"), "<app-root></app-root>");
            Files.writeString(pasta.resolve("main-ABC123.js"), "console.log('app')");
            return pasta;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
