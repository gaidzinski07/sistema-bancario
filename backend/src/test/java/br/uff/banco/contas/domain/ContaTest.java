package br.uff.banco.contas.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.uff.banco.agencias.domain.Agencia;
import br.uff.banco.clientes.domain.Cliente;
import br.uff.banco.clientes.domain.SegmentoCliente;
import br.uff.banco.identidade.domain.Usuario;
import br.uff.banco.shared.domain.Cpf;
import br.uff.banco.shared.domain.Dinheiro;
import br.uff.banco.shared.domain.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ContaTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        // mesmos dígitos do seed de desenvolvimento
        "00001, 9",
        "00002, 7",
        "00003, 5",
        "00004, 3",
        "00005, 1",
        "00006, 0",   // soma 12, resto 1 -> 11 - 1 = 10 -> vira 0
        "12345, 5"    // 5*2 + 4*3 + 3*4 + 2*5 + 1*6 = 50, resto 6 -> 11 - 6 = 5
    })
    void calculaDigitoModulo11(String numero, int digito) {
        assertThat(Conta.calcularDigito(numero)).isEqualTo(digito);
    }

    @Test
    void formataNumeroComZerosAEsquerda() {
        assertThat(Conta.formatarNumero(7)).isEqualTo("00007");
        assertThat(Conta.formatarNumero(99999)).isEqualTo("99999");
    }

    @Test
    void recusaNumeroForaDaFaixa() {
        assertThatThrownBy(() -> Conta.formatarNumero(100000))
                .isInstanceOf(RegraNegocioException.class)
                .extracting("codigo").isEqualTo("numeracao-esgotada");
        assertThatThrownBy(() -> Conta.formatarNumero(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void abreContaAtivaComSaldoZero() {
        var conta = Conta.abrir(agencia(), 42, cliente(), TipoConta.POUPANCA);

        assertThat(conta.getNumero()).isEqualTo("00042");
        assertThat(conta.getDigito()).isEqualTo(String.valueOf(Conta.calcularDigito("00042")));
        assertThat(conta.getSaldo()).isEqualTo(Dinheiro.ZERO);
        assertThat(conta.getStatus()).isEqualTo(StatusConta.ATIVA);
        assertThat(conta.getTipo()).isEqualTo(TipoConta.POUPANCA);
    }

    @Test
    void naoAbreContaParaClienteInativo() {
        var cliente = cliente();
        cliente.getUsuario().desativar();

        assertThatThrownBy(() -> Conta.abrir(agencia(), 1, cliente, TipoConta.CORRENTE))
                .isInstanceOf(RegraNegocioException.class)
                .extracting("codigo").isEqualTo("cliente-inativo");
    }

    private static Agencia agencia() {
        return new Agencia("0001", "Centro", null);
    }

    private static Cliente cliente() {
        var usuario = Usuario.novo(Cpf.of("52998224725"), "Fulano", null, null, "{noop}x");
        return Cliente.de(usuario, SegmentoCliente.CORRENTISTA, null);
    }
}
