package br.uff.banco.contas.domain;

import br.uff.banco.shared.domain.Dinheiro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

/** Saque. Imutável. */
@Entity
@Table(name = "saque")
public class Saque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    @Column(name = "valor", nullable = false, precision = 19, scale = 2)
    private Dinheiro valor;

    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    protected Saque() {
        // JPA
    }

    public Saque(Conta conta, Dinheiro valor, Instant ocorridoEm) {
        this.conta = Objects.requireNonNull(conta);
        this.valor = Objects.requireNonNull(valor).exigirPositivo();
        this.ocorridoEm = Objects.requireNonNull(ocorridoEm);
    }

    public Movimento movimento() {
        return new Movimento(TipoLancamento.SAQUE, valor, "Saque", null, id, ocorridoEm);
    }

    public Long getId() {
        return id;
    }

    public Conta getConta() {
        return conta;
    }

    public Dinheiro getValor() {
        return valor;
    }

    public Instant getOcorridoEm() {
        return ocorridoEm;
    }
}
