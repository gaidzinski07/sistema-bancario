package br.uff.banco.contas.domain;

import br.uff.banco.shared.domain.Dinheiro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

/**
 * Depósito em conta. Imutável.
 * <p>
 * A coluna {@code agencia_id} (agência física onde o depósito foi feito) fica nula
 * nas operações pela internet.
 */
@Entity
@Table(name = "deposito")
public class Deposito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma", nullable = false, length = 20)
    private FormaDeposito forma;

    @Column(name = "valor", nullable = false, precision = 19, scale = 2)
    private Dinheiro valor;

    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    protected Deposito() {
        // JPA
    }

    public Deposito(Conta conta, FormaDeposito forma, Dinheiro valor, Instant ocorridoEm) {
        this.conta = Objects.requireNonNull(conta);
        this.forma = Objects.requireNonNull(forma);
        this.valor = Objects.requireNonNull(valor).exigirPositivo();
        this.ocorridoEm = Objects.requireNonNull(ocorridoEm);
    }

    public Movimento movimento() {
        String descricao = forma == FormaDeposito.CHEQUE ? "Depósito em cheque" : "Depósito em dinheiro";
        return new Movimento(TipoLancamento.DEPOSITO, valor, descricao, null, id, ocorridoEm);
    }

    public Long getId() {
        return id;
    }

    public Conta getConta() {
        return conta;
    }

    public FormaDeposito getForma() {
        return forma;
    }

    public Dinheiro getValor() {
        return valor;
    }

    public Instant getOcorridoEm() {
        return ocorridoEm;
    }
}
