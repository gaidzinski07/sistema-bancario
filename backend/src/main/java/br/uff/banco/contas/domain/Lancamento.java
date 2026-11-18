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

/**
 * Linha do extrato. Imutável.
 * <p>
 * Só é criado por {@link Conta#creditar} e {@link Conta#debitar}: não existe
 * alteração de saldo sem lançamento, e por isso a soma dos lançamentos de uma
 * conta é sempre igual ao saldo. No legado o extrato era um UNION de quatro
 * tabelas e o saldo era alterado por UPDATE solto.
 */
@Entity
@Table(name = "lancamento")
public class Lancamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoLancamento tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "natureza", nullable = false, length = 10)
    private Natureza natureza;

    @Column(name = "valor", nullable = false, precision = 19, scale = 2)
    private Dinheiro valor;

    @Column(name = "saldo_apos", nullable = false, precision = 19, scale = 2)
    private Dinheiro saldoApos;

    @Column(name = "descricao", nullable = false, length = 200)
    private String descricao;

    @Column(name = "contraparte", length = 150)
    private String contraparte;

    @Column(name = "operacao_id")
    private Long operacaoId;

    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    protected Lancamento() {
        // JPA
    }

    Lancamento(Conta conta, Natureza natureza, Dinheiro saldoApos, Movimento movimento) {
        this.conta = conta;
        this.tipo = movimento.tipo();
        this.natureza = natureza;
        this.valor = movimento.valor();
        this.saldoApos = saldoApos;
        this.descricao = movimento.descricao();
        this.contraparte = movimento.contraparte();
        this.operacaoId = movimento.operacaoId();
        this.ocorridoEm = movimento.ocorridoEm();
    }

    public Long getId() {
        return id;
    }

    public Conta getConta() {
        return conta;
    }

    public TipoLancamento getTipo() {
        return tipo;
    }

    public Natureza getNatureza() {
        return natureza;
    }

    public Dinheiro getValor() {
        return valor;
    }

    public Dinheiro getSaldoApos() {
        return saldoApos;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getContraparte() {
        return contraparte;
    }

    public Long getOperacaoId() {
        return operacaoId;
    }

    public Instant getOcorridoEm() {
        return ocorridoEm;
    }
}
