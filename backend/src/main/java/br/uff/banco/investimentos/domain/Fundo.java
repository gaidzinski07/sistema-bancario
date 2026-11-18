package br.uff.banco.investimentos.domain;

import br.uff.banco.shared.domain.Dinheiro;
import br.uff.banco.shared.domain.RegraNegocioException;
import br.uff.banco.shared.persistence.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Fundo de investimento.
 * <p>
 * A cota é atualizada pelo administrador ({@link #atualizarCota}); no legado era um
 * valor fixo no banco, alterado por UPDATE manual. Fundo inativo não aceita novas
 * aplicações, mas continua permitindo resgates.
 */
@Entity
@Table(name = "fundo")
public class Fundo extends EntidadeBase {

    @Column(name = "nome", nullable = false, unique = true, length = 100)
    private String nome;

    @Column(name = "cota_atual", nullable = false, precision = 19, scale = Cotas.ESCALA)
    private BigDecimal cotaAtual;

    @Column(name = "aplicacao_minima", nullable = false, precision = 19, scale = 2)
    private Dinheiro aplicacaoMinima;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    protected Fundo() {
        // JPA
    }

    public Fundo(String nome, BigDecimal cotaInicial, Dinheiro aplicacaoMinima) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("campo-obrigatorio", "O nome do fundo é obrigatório.");
        }
        this.nome = nome.trim();
        this.cotaAtual = exigirCotaValida(cotaInicial);
        this.aplicacaoMinima = Objects.requireNonNull(aplicacaoMinima, "aplicacaoMinima");
        if (aplicacaoMinima.isNegativo()) {
            throw new RegraNegocioException("valor-invalido", "A aplicação mínima não pode ser negativa.");
        }
    }

    public void atualizarCota(BigDecimal novaCota) {
        this.cotaAtual = exigirCotaValida(novaCota);
    }

    public void desativar() {
        this.ativo = false;
    }

    /** Fundo ativo e valor acima do mínimo. */
    public void exigirAceitaAplicacao(Dinheiro valor) {
        if (!ativo) {
            throw new RegraNegocioException("fundo-inativo", "O fundo " + nome + " não aceita novas aplicações.");
        }
        if (valor.isMenorQue(aplicacaoMinima)) {
            throw new RegraNegocioException("abaixo-da-aplicacao-minima",
                    "A aplicação mínima do fundo " + nome + " é R$ " + aplicacaoMinima + ".");
        }
    }

    static BigDecimal exigirCotaValida(BigDecimal cota) {
        if (!Cotas.positiva(cota)) {
            throw new RegraNegocioException("cota-invalida", "O valor da cota deve ser maior que zero.");
        }
        if (cota.stripTrailingZeros().scale() > Cotas.ESCALA) {
            throw new RegraNegocioException("cota-invalida", "O valor da cota pode ter no máximo " + Cotas.ESCALA + " casas decimais.");
        }
        return Cotas.normalizar(cota);
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getCotaAtual() {
        return cotaAtual;
    }

    public Dinheiro getAplicacaoMinima() {
        return aplicacaoMinima;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
