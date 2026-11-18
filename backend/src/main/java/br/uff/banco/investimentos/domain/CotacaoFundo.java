package br.uff.banco.investimentos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** Histórico diário da cota de um fundo (uma linha por dia; a última atualização do dia vale). */
@Entity
@Table(name = "cotacao_fundo")
public class CotacaoFundo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fundo_id", nullable = false)
    private Fundo fundo;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "valor_cota", nullable = false, precision = 19, scale = Cotas.ESCALA)
    private BigDecimal valorCota;

    protected CotacaoFundo() {
        // JPA
    }

    public CotacaoFundo(Fundo fundo, LocalDate data, BigDecimal valorCota) {
        this.fundo = Objects.requireNonNull(fundo);
        this.data = Objects.requireNonNull(data);
        this.valorCota = Fundo.exigirCotaValida(valorCota);
    }

    public void corrigir(BigDecimal novoValor) {
        this.valorCota = Fundo.exigirCotaValida(novoValor);
    }

    public LocalDate getData() {
        return data;
    }

    public BigDecimal getValorCota() {
        return valorCota;
    }
}
