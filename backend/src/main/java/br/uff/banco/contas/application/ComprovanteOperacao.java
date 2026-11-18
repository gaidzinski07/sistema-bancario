package br.uff.banco.contas.application;

import br.uff.banco.contas.domain.Conta;
import br.uff.banco.contas.domain.Lancamento;
import br.uff.banco.contas.domain.TipoLancamento;
import java.math.BigDecimal;
import java.time.Instant;

/** Comprovante de depósito ou saque. */
public record ComprovanteOperacao(
        Long operacaoId,
        TipoLancamento tipo,
        String descricao,
        BigDecimal valor,
        BigDecimal saldoApos,
        Instant ocorridoEm,
        ContaResumo conta) {

    static ComprovanteOperacao de(Lancamento lancamento) {
        return new ComprovanteOperacao(
                lancamento.getOperacaoId(),
                lancamento.getTipo(),
                lancamento.getDescricao(),
                lancamento.getValor().valor(),
                lancamento.getSaldoApos().valor(),
                lancamento.getOcorridoEm(),
                ContaResumo.de(lancamento.getConta()));
    }

    public record ContaResumo(Long id, String agencia, String conta, String tipo) {

        static ContaResumo de(Conta conta) {
            return new ContaResumo(conta.getId(), conta.getAgencia().getCodigo(), conta.getNumeroComDigito(), conta.getTipo().name());
        }
    }
}
