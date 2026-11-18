package br.uff.banco.identidade.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.uff.banco.clientes.domain.Cliente;
import br.uff.banco.clientes.domain.SegmentoCliente;
import br.uff.banco.shared.domain.Cpf;
import br.uff.banco.shared.domain.RegraNegocioException;
import org.junit.jupiter.api.Test;

class UsuarioTest {

    @Test
    void nasceSemPapeisEAtivo() {
        var usuario = novo();

        assertThat(usuario.getPapeis()).isEmpty();
        assertThat(usuario.isAtivo()).isTrue();
    }

    @Test
    void normalizaEmail() {
        var usuario = Usuario.novo(Cpf.of("52998224725"), " Fulano ", " Fulano@Banco.TEST ", null, "{noop}x");

        assertThat(usuario.getEmail()).isEqualTo("fulano@banco.test");
        assertThat(usuario.getNome()).isEqualTo("Fulano");
    }

    @Test
    void administradorRecebePapelEPerfil() {
        var usuario = novo();
        usuario.tornarAdministrador(PerfilAdministrador.GERENTE);

        assertThat(usuario.getPapeis()).containsExactly(Papel.ADMIN);
        assertThat(usuario.getPerfilAdministrador()).isEqualTo(PerfilAdministrador.GERENTE);
    }

    @Test
    void mesmaPessoaPodeSerAdminECliente() {
        var usuario = novo();
        usuario.tornarAdministrador(PerfilAdministrador.OPERADOR);
        Cliente.de(usuario, SegmentoCliente.INVESTIDOR, null);

        assertThat(usuario.getPapeis()).containsExactlyInAnyOrder(Papel.ADMIN, Papel.CLIENTE);
    }

    @Test
    void naoViraClienteDuasVezes() {
        var usuario = novo();
        Cliente.de(usuario, SegmentoCliente.CORRENTISTA, null);

        assertThatThrownBy(() -> Cliente.de(usuario, SegmentoCliente.CORRENTISTA, null))
                .isInstanceOf(RegraNegocioException.class)
                .extracting("codigo").isEqualTo("ja-e-cliente");
    }

    @Test
    void exigeNome() {
        assertThatThrownBy(() -> Usuario.novo(Cpf.of("52998224725"), "  ", null, null, "{noop}x"))
                .isInstanceOf(RegraNegocioException.class);
    }

    private static Usuario novo() {
        return Usuario.novo(Cpf.of("52998224725"), "Fulano", null, null, "{noop}x");
    }
}
