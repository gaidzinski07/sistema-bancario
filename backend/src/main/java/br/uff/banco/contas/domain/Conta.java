package br.uff.banco.contas.domain;

import br.uff.banco.agencias.domain.Agencia;
import br.uff.banco.clientes.domain.Cliente;
import br.uff.banco.shared.domain.Dinheiro;
import br.uff.banco.shared.domain.RegraNegocioException;
import br.uff.banco.shared.persistence.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.Objects;

/**
 * Conta bancária (corrente ou poupança) de um titular, numa agência.
 * <p>
 * Nesta fase a conta só é aberta; débito e crédito (com lock e lançamentos)
 * entram na Fase 3.
 */
@Entity
@Table(name = "conta")
public class Conta extends EntidadeBase {

    static final int TAMANHO_NUMERO = 5;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agencia_id", nullable = false)
    private Agencia agencia;

    @Column(name = "numero", nullable = false, length = 10)
    private String numero;

    @Column(name = "digito", nullable = false, length = 1)
    private String digito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "titular_id", nullable = false)
    private Cliente titular;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoConta tipo;

    @Column(name = "saldo", nullable = false, precision = 19, scale = 2)
    private Dinheiro saldo = Dinheiro.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusConta status = StatusConta.ATIVA;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Conta() {
        // JPA
    }

    private Conta(Agencia agencia, String numero, Cliente titular, TipoConta tipo) {
        this.agencia = agencia;
        this.numero = numero;
        this.digito = String.valueOf(calcularDigito(numero));
        this.titular = titular;
        this.tipo = tipo;
    }

    /**
     * Abre uma conta com saldo zero.
     *
     * @param numeroSequencial número reservado na agência ({@link Agencia#reservarNumeroConta()})
     */
    public static Conta abrir(Agencia agencia, long numeroSequencial, Cliente titular, TipoConta tipo) {
        Objects.requireNonNull(agencia, "agencia");
        Objects.requireNonNull(titular, "titular");
        Objects.requireNonNull(tipo, "tipo");
        if (!titular.isAtivo()) {
            throw new RegraNegocioException("cliente-inativo", "Não é possível abrir conta para um cliente inativo.");
        }
        return new Conta(agencia, formatarNumero(numeroSequencial), titular, tipo);
    }

    static String formatarNumero(long sequencial) {
        if (sequencial < 1) {
            throw new IllegalArgumentException("Número de conta deve ser positivo: " + sequencial);
        }
        String numero = String.format("%0" + TAMANHO_NUMERO + "d", sequencial);
        if (numero.length() > TAMANHO_NUMERO) {
            throw new RegraNegocioException("numeracao-esgotada", "A numeração de contas da agência se esgotou.");
        }
        return numero;
    }

    /** Dígito verificador módulo 11: pesos 2..9 da direita para a esquerda; resto ≥ 10 vira 0. */
    static int calcularDigito(String numero) {
        int soma = 0;
        int peso = 2;
        for (int i = numero.length() - 1; i >= 0; i--) {
            soma += (numero.charAt(i) - '0') * peso;
            peso = peso == 9 ? 2 : peso + 1;
        }
        int digito = 11 - (soma % 11);
        return digito >= 10 ? 0 : digito;
    }

    public String getNumeroComDigito() {
        return numero + "-" + digito;
    }

    public Agencia getAgencia() {
        return agencia;
    }

    public String getNumero() {
        return numero;
    }

    public String getDigito() {
        return digito;
    }

    public Cliente getTitular() {
        return titular;
    }

    public TipoConta getTipo() {
        return tipo;
    }

    public Dinheiro getSaldo() {
        return saldo;
    }

    public StatusConta getStatus() {
        return status;
    }
}
