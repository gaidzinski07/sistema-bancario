package br.uff.banco.investimentos.domain;

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
import java.math.BigDecimal;
import java.time.Instant;

/** Aplicação ou resgate numa posição. Imutável; vira um lançamento na conta corrente. */
@Entity
@Table(name = "movimento_investimento")
public class MovimentoInvestimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "posicao_id", nullable = false)
    private Posicao posicao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoMovimentoInvestimento tipo;

    @Column(name = "quantidade_cotas", nullable = false, precision = 19, scale = Cotas.ESCALA)
    private BigDecimal quantidadeCotas;

    @Column(name = "valor_cota", nullable = false, precision = 19, scale = Cotas.ESCALA)
    private BigDecimal valorCota;

    @Column(name = "valor", nullable = false, precision = 19, scale = 2)
    private Dinheiro valor;

    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    protected MovimentoInvestimento() {
        // JPA
    }

    MovimentoInvestimento(Posicao posicao, TipoMovimentoInvestimento tipo, BigDecimal quantidadeCotas,
            BigDecimal valorCota, Dinheiro valor, Instant ocorridoEm) {
        this.posicao = posicao;
        this.tipo = tipo;
        this.quantidadeCotas = quantidadeCotas;
        this.valorCota = valorCota;
        this.valor = valor;
        this.ocorridoEm = ocorridoEm;
    }

    public Long getId() {
        return id;
    }

    public Posicao getPosicao() {
        return posicao;
    }

    public TipoMovimentoInvestimento getTipo() {
        return tipo;
    }

    public BigDecimal getQuantidadeCotas() {
        return quantidadeCotas;
    }

    public BigDecimal getValorCota() {
        return valorCota;
    }

    public Dinheiro getValor() {
        return valor;
    }

    public Instant getOcorridoEm() {
        return ocorridoEm;
    }
}
