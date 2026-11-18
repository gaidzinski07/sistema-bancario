package br.uff.banco;

import static org.assertj.core.api.Assertions.assertThat;

import br.uff.banco.agencias.domain.Agencia;
import br.uff.banco.clientes.domain.Cliente;
import br.uff.banco.clientes.domain.SegmentoCliente;
import br.uff.banco.contas.domain.Conta;
import br.uff.banco.contas.domain.TipoConta;
import br.uff.banco.identidade.domain.Papel;
import br.uff.banco.identidade.domain.PerfilAdministrador;
import br.uff.banco.identidade.domain.Usuario;
import br.uff.banco.shared.domain.Cpf;
import br.uff.banco.shared.domain.Dinheiro;
import br.uff.banco.shared.domain.Endereco;
import br.uff.banco.suporte.Cpfs;
import br.uff.banco.suporte.MySqlTestcontainersConfig;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * Mapeamento ORM contra o schema real do Flyway ({@code ddl-auto=validate} já
 * confere colunas e tipos ao subir; aqui se confere ida e volta dos dados).
 */
@DataJpaTest
@Import(MySqlTestcontainersConfig.class)
@EnabledIf(value = "br.uff.banco.suporte.Docker#disponivel", disabledReason = "Docker indisponível")
class MapeamentoJpaIT {

    @Autowired
    EntityManager em;

    @Test
    void persisteERecarregaUsuarioClienteEConta() {
        var usuario = Usuario.novo(Cpf.of(Cpfs.novo()), "Fulana de Tal", "fulana@banco.test", LocalDate.of(1990, 1, 2), "{noop}x");
        usuario.tornarAdministrador(PerfilAdministrador.GERENTE);
        em.persist(usuario);
        var endereco = new Endereco("Rua A", "10", null, "Centro", "Niterói", "rj", "24020-000");
        var cliente = Cliente.de(usuario, SegmentoCliente.INVESTIDOR, endereco);
        em.persist(cliente);
        var agencia = new Agencia("7777", "Agência Mapeamento", null);
        em.persist(agencia);
        var conta = Conta.abrir(agencia, agencia.reservarNumeroConta(), cliente, TipoConta.CORRENTE);
        em.persist(conta);
        em.flush();
        em.clear();

        Cliente recarregado = em.find(Cliente.class, usuario.getId());
        assertThat(recarregado.getId()).as("@MapsId: PK do cliente = PK do usuário").isEqualTo(usuario.getId());
        assertThat(recarregado.getSegmento()).isEqualTo(SegmentoCliente.INVESTIDOR);
        assertThat(recarregado.getEndereco()).isEqualTo(new Endereco("Rua A", "10", null, "Centro", "Niterói", "RJ", "24020000"));
        assertThat(recarregado.getCriadoEm()).isNotNull();
        assertThat(recarregado.getUsuario().getCpf()).isEqualTo(usuario.getCpf());
        assertThat(recarregado.getUsuario().getPapeis()).containsExactlyInAnyOrder(Papel.ADMIN, Papel.CLIENTE);

        Conta contaRecarregada = em.find(Conta.class, conta.getId());
        assertThat(Hibernate.isInitialized(contaRecarregada.getAgencia())).as("agência é LAZY").isFalse();
        assertThat(contaRecarregada.getSaldo()).isEqualTo(Dinheiro.ZERO);
        assertThat(contaRecarregada.getNumeroComDigito()).isEqualTo("00001-9");
        assertThat(contaRecarregada.getTitular()).isEqualTo(recarregado);
    }

    @Test
    void igualdadeFuncionaEntreProxyEEntidadeCarregada() {
        var agencia = new Agencia("7778", "Agência Proxy", null);
        em.persist(agencia);
        em.flush();
        em.clear();

        Agencia proxy = em.getReference(Agencia.class, agencia.getId());
        Agencia carregada = em.find(Agencia.class, agencia.getId());
        em.clear();
        Agencia outroProxy = em.getReference(Agencia.class, agencia.getId());

        assertThat(carregada).isEqualTo(proxy);
        assertThat(outroProxy).isEqualTo(carregada);
        assertThat(outroProxy.hashCode()).isEqualTo(carregada.hashCode());
    }
}
