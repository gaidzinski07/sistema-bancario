package br.uff.banco.identidade.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.uff.banco.shared.domain.Cpf;
import br.uff.banco.shared.domain.RegraNegocioException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SenhaTransacaoTest {

    private static final Instant T0 = Instant.parse("2026-11-18T12:00:00Z");

    @Test
    void semSenhaDefinidaNaoOpera() {
        var usuario = Usuario.novo(Cpf.of("52998224725"), "Fulano", null, null, "{noop}x");

        assertThatThrownBy(() -> usuario.exigirSenhaTransacaoDisponivel(T0))
                .extracting("codigo").isEqualTo("senha-transacao-nao-definida");
    }

    @Test
    void errosContamTentativasRestantes() {
        var usuario = comSenha();

        assertThatThrownBy(() -> usuario.registrarTentativaSenhaTransacao(false, T0))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("2 tentativas restantes");
        assertThatThrownBy(() -> usuario.registrarTentativaSenhaTransacao(false, T0))
                .hasMessageContaining("1 tentativa restante");
    }

    @Test
    void terceiroErroBloqueiaInclusiveParaASenhaCerta() {
        var usuario = comSenha();
        errar(usuario, 3);

        assertThatThrownBy(() -> usuario.exigirSenhaTransacaoDisponivel(T0.plusSeconds(60)))
                .extracting("codigo").isEqualTo("senha-transacao-bloqueada");
    }

    @Test
    void bloqueioExpiraDepoisDe15Minutos() {
        var usuario = comSenha();
        errar(usuario, 3);

        assertThatThrownBy(() -> usuario.exigirSenhaTransacaoDisponivel(T0.plus(Usuario.BLOQUEIO_SENHA_TRANSACAO).minusSeconds(1)))
                .extracting("codigo").isEqualTo("senha-transacao-bloqueada");
        assertThatCode(() -> usuario.exigirSenhaTransacaoDisponivel(T0.plus(Usuario.BLOQUEIO_SENHA_TRANSACAO)))
                .doesNotThrowAnyException();
    }

    @Test
    void acertoZeraOContador() {
        var usuario = comSenha();
        errar(usuario, 2);
        usuario.registrarTentativaSenhaTransacao(true, T0);

        // depois do acerto, são necessários 3 novos erros para bloquear
        errar(usuario, 2);
        assertThatCode(() -> usuario.exigirSenhaTransacaoDisponivel(T0)).doesNotThrowAnyException();
    }

    @Test
    void redefinirASenhaDesbloqueia() {
        var usuario = comSenha();
        errar(usuario, 3);

        usuario.definirSenhaTransacao("{noop}9999");

        assertThatCode(() -> usuario.exigirSenhaTransacaoDisponivel(T0)).doesNotThrowAnyException();
    }

    private static Usuario comSenha() {
        var usuario = Usuario.novo(Cpf.of("52998224725"), "Fulano", null, null, "{noop}x");
        usuario.definirSenhaTransacao("{noop}1234");
        return usuario;
    }

    private static void errar(Usuario usuario, int vezes) {
        for (int i = 0; i < vezes; i++) {
            try {
                usuario.registrarTentativaSenhaTransacao(false, T0);
            } catch (RegraNegocioException esperado) {
                // cada erro lança; o estado é o que interessa aqui
            }
        }
    }
}
