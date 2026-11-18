package br.uff.banco.suporte;

import org.testcontainers.DockerClientFactory;

/** Condição usada por {@link TesteDeIntegracao}: sem Docker, os testes de integração são pulados. */
public final class Docker {

    private Docker() {
    }

    public static boolean disponivel() {
        return DockerClientFactory.instance().isDockerAvailable();
    }
}
