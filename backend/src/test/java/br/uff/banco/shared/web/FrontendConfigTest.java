package br.uff.banco.shared.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/** Resolução dos arquivos do frontend servidos pela API (ver {@link FrontendConfig}). */
class FrontendConfigTest {

    @TempDir
    Path build;

    Resource local;
    final FrontendConfig.ArquivoOuIndex resolvedor = new FrontendConfig.ArquivoOuIndex();

    @BeforeEach
    void criarBuild() throws IOException {
        Files.writeString(build.resolve("index.html"), "<app-root></app-root>");
        Files.writeString(build.resolve("main-ABC123.js"), "console.log('app')");
        local = new FileSystemResource(build.toString() + "/");
    }

    @Test
    void arquivoExistenteEhServidoComoEsta() throws IOException {
        assertThat(resolvedor.getResource("main-ABC123.js", local).getFilename()).isEqualTo("main-ABC123.js");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "inicio", "contas/5/extrato", "admin/clientes/novo"})
    void rotasDoAngularDevolvemOIndex(String rota) throws IOException {
        assertThat(resolvedor.getResource(rota, local).getFilename()).isEqualTo("index.html");
    }

    @ParameterizedTest
    @ValueSource(strings = {"api/nao-existe", "api/contas/999", "actuator/env", "nao-existe.js", "favicon.ico"})
    void apiActuatorEArquivosInexistentesNaoCaemNoIndex(String caminho) throws IOException {
        assertThat(resolvedor.getResource(caminho, local)).isNull();
    }
}
