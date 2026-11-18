package br.uff.banco.contas.application;

import br.uff.banco.contas.domain.Lancamento;
import br.uff.banco.contas.domain.Natureza;
import br.uff.banco.contas.domain.TipoLancamento;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Linha do extrato.
 *
 * @param valor sempre positivo; a {@code natureza} diz se entrou ou saiu (no legado, a tela
 *              mostrava o mesmo valor nas colunas "Entrada" e "Saída")
 */
public record LancamentoView(
        Long id,
        Instant ocorridoEm,
        TipoLancamento tipo,
        Natureza natureza,
        String descricao,
        String contraparte,
        BigDecimal valor,
        BigDecimal saldoApos) {

    static LancamentoView de(Lancamento lancamento) {
        return new LancamentoView(
                lancamento.getId(),
                lancamento.getOcorridoEm(),
                lancamento.getTipo(),
                lancamento.getNatureza(),
                lancamento.getDescricao(),
                lancamento.getContraparte(),
                lancamento.getValor().valor(),
                lancamento.getSaldoApos().valor());
    }
}
