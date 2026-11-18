package br.uff.banco.investimentos.domain;

import br.uff.banco.contas.domain.Conta;
import br.uff.banco.contas.domain.TipoConta;
import br.uff.banco.shared.domain.Dinheiro;
import br.uff.banco.shared.domain.RegraNegocioException;
import br.uff.banco.shared.persistence.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

/**
 * Posição de uma conta corrente num fundo: quantas cotas tem e quanto custaram.
 * <p>
 * Substitui a tabela INVESTIMENTO do legado (uma linha por aplicação, só resgate
 * total, rendimento por regra de três sobre o valor investido ignorando as cotas).
 * <p>
 * Concorrência: toda alteração de posição acontece com a conta dona travada
 * (aplicar e resgatar movimentam o saldo dela), então a conta serializa as
 * operações sobre a posição; o {@code @Version} é a segunda proteção.
 */
@Entity
@Table(name = "posicao")
public class Posicao extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fundo_id", nullable = false)
    private Fundo fundo;

    @Column(name = "quantidade_cotas", nullable = false, precision = 19, scale = Cotas.ESCALA)
    private BigDecimal quantidadeCotas = Cotas.normalizar(BigDecimal.ZERO);

    /** Custo de aquisição das cotas em carteira (base do rendimento). */
    @Column(name = "valor_aplicado", nullable = false, precision = 19, scale = 2)
    private Dinheiro valorAplicado = Dinheiro.ZERO;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Posicao() {
        // JPA
    }

    private Posicao(Conta conta, Fundo fundo) {
        this.conta = conta;
        this.fundo = fundo;
    }

    /** Investimentos saem e voltam para a conta corrente (decisão 4 do plano). */
    public static Posicao abrir(Conta conta, Fundo fundo) {
        Objects.requireNonNull(conta, "conta");
        Objects.requireNonNull(fundo, "fundo");
        if (conta.getTipo() != TipoConta.CORRENTE) {
            throw new RegraNegocioException("conta-nao-corrente", "Aplicações são feitas a partir da conta corrente.");
        }
        return new Posicao(conta, fundo);
    }

    /**
     * Compra cotas pela cota atual do fundo. O débito na conta é feito por quem
     * chama, com o valor do movimento devolvido.
     */
    public MovimentoInvestimento aplicar(Dinheiro valor, Instant agora) {
        valor.exigirPositivo();
        fundo.exigirAceitaAplicacao(valor);
        BigDecimal cota = fundo.getCotaAtual();
        BigDecimal cotas = valor.valor().divide(cota, Cotas.ESCALA, RoundingMode.DOWN);
        if (cotas.signum() == 0) {
            throw new RegraNegocioException("valor-invalido", "O valor não compra nenhuma fração de cota.");
        }
        quantidadeCotas = quantidadeCotas.add(cotas);
        valorAplicado = valorAplicado.somar(valor);
        return new MovimentoInvestimento(this, TipoMovimentoInvestimento.APLICACAO, cotas, cota, valor, agora);
    }

    /**
     * Resgate parcial: vende as cotas necessárias para pagar {@code valor} (arredondando
     * as cotas para cima). Se isso consumir todas as cotas, vira resgate total.
     */
    public MovimentoInvestimento resgatar(Dinheiro valor, Instant agora) {
        valor.exigirPositivo();
        Dinheiro disponivel = valorAtual();
        if (disponivel.isMenorQue(valor)) {
            throw new RegraNegocioException("saldo-investido-insuficiente",
                    "O valor disponível para resgate neste fundo é R$ " + disponivel + ".");
        }
        BigDecimal cota = fundo.getCotaAtual();
        BigDecimal cotas = valor.valor().divide(cota, Cotas.ESCALA, RoundingMode.UP);
        return baixar(cotas.compareTo(quantidadeCotas) >= 0 ? quantidadeCotas : cotas, valor, cota, agora);
    }

    /** Resgate total: vende todas as cotas pelo valor atual da posição. */
    public MovimentoInvestimento resgatarTudo(Instant agora) {
        Dinheiro disponivel = valorAtual();
        if (!disponivel.isPositivo()) {
            throw new RegraNegocioException("posicao-vazia", "Não há valor a resgatar nesta posição.");
        }
        return baixar(quantidadeCotas, disponivel, fundo.getCotaAtual(), agora);
    }

    /** Tira as cotas da posição e baixa o custo proporcionalmente (custo médio). */
    private MovimentoInvestimento baixar(BigDecimal cotas, Dinheiro valorPago, BigDecimal cota, Instant agora) {
        Dinheiro custoBaixado = cotas.compareTo(quantidadeCotas) == 0
                ? valorAplicado
                : Dinheiro.of(valorAplicado.valor().multiply(cotas).divide(quantidadeCotas, 2, RoundingMode.HALF_EVEN));
        quantidadeCotas = quantidadeCotas.subtract(cotas);
        valorAplicado = valorAplicado.subtrair(custoBaixado);
        return new MovimentoInvestimento(this, TipoMovimentoInvestimento.RESGATE, cotas, cota, valorPago, agora);
    }

    /** Quanto a posição vale hoje: cotas × cota atual, truncado no centavo. */
    public Dinheiro valorAtual() {
        return Dinheiro.of(quantidadeCotas.multiply(fundo.getCotaAtual()).setScale(2, RoundingMode.DOWN));
    }

    /** Valor atual − custo das cotas em carteira (pode ser negativo). */
    public Dinheiro rendimento() {
        return valorAtual().subtrair(valorAplicado);
    }

    public boolean isZerada() {
        return quantidadeCotas.signum() == 0;
    }

    public boolean pertenceA(Long clienteId) {
        return conta.pertenceA(clienteId);
    }

    public Conta getConta() {
        return conta;
    }

    public Fundo getFundo() {
        return fundo;
    }

    public BigDecimal getQuantidadeCotas() {
        return quantidadeCotas;
    }

    public Dinheiro getValorAplicado() {
        return valorAplicado;
    }
}
