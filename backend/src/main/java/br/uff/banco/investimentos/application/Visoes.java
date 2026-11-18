package br.uff.banco.investimentos.application;

import br.uff.banco.contas.application.ContaResumo;
import br.uff.banco.investimentos.domain.CotacaoFundo;
import br.uff.banco.investimentos.domain.Fundo;
import br.uff.banco.investimentos.domain.MovimentoInvestimento;
import br.uff.banco.investimentos.domain.Posicao;
import br.uff.banco.investimentos.domain.TipoMovimentoInvestimento;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Respostas do módulo de investimentos. */
public final class Visoes {

    private Visoes() {
    }

    public record FundoView(Long id, String nome, BigDecimal cotaAtual, BigDecimal aplicacaoMinima, boolean ativo) {

        static FundoView de(Fundo fundo) {
            return new FundoView(fundo.getId(), fundo.getNome(), fundo.getCotaAtual(), fundo.getAplicacaoMinima().valor(),
                    fundo.isAtivo());
        }
    }

    public record FundoResumo(Long id, String nome) {

        static FundoResumo de(Fundo fundo) {
            return new FundoResumo(fundo.getId(), fundo.getNome());
        }
    }

    public record CotacaoView(LocalDate data, BigDecimal valorCota) {

        static CotacaoView de(CotacaoFundo cotacao) {
            return new CotacaoView(cotacao.getData(), cotacao.getValorCota());
        }
    }

    /**
     * Posição na carteira.
     *
     * @param valorAtual             cotas × cota atual (truncado no centavo): é o que o resgate total paga
     * @param rendimento             valor atual − valor aplicado
     * @param rentabilidadePercentual rendimento ÷ valor aplicado, em %; nulo se não há custo
     */
    public record PosicaoView(
            Long id,
            FundoResumo fundo,
            ContaResumo conta,
            BigDecimal quantidadeCotas,
            BigDecimal cotaAtual,
            BigDecimal valorAplicado,
            BigDecimal valorAtual,
            BigDecimal rendimento,
            BigDecimal rentabilidadePercentual) {

        static PosicaoView de(Posicao posicao) {
            BigDecimal aplicado = posicao.getValorAplicado().valor();
            BigDecimal rendimento = posicao.rendimento().valor();
            BigDecimal rentabilidade = aplicado.signum() == 0
                    ? null
                    : rendimento.multiply(BigDecimal.valueOf(100)).divide(aplicado, 2, RoundingMode.HALF_EVEN);
            return new PosicaoView(
                    posicao.getId(),
                    FundoResumo.de(posicao.getFundo()),
                    ContaResumo.de(posicao.getConta()),
                    posicao.getQuantidadeCotas(),
                    posicao.getFundo().getCotaAtual(),
                    aplicado,
                    posicao.valorAtual().valor(),
                    rendimento,
                    rentabilidade);
        }
    }

    public record MovimentoView(
            Long id,
            TipoMovimentoInvestimento tipo,
            BigDecimal quantidadeCotas,
            BigDecimal valorCota,
            BigDecimal valor,
            Instant ocorridoEm) {

        static MovimentoView de(MovimentoInvestimento movimento) {
            return new MovimentoView(movimento.getId(), movimento.getTipo(), movimento.getQuantidadeCotas(),
                    movimento.getValorCota(), movimento.getValor().valor(), movimento.getOcorridoEm());
        }
    }

    public record DetalhePosicaoView(PosicaoView posicao, List<MovimentoView> movimentos) {
    }

    /** Comprovante de aplicação ou resgate. */
    public record ComprovanteInvestimento(
            MovimentoView movimento,
            FundoResumo fundo,
            ContaResumo conta,
            BigDecimal saldoContaApos,
            PosicaoView posicao) {
    }
}
