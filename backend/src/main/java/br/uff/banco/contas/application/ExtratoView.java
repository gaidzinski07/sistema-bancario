package br.uff.banco.contas.application;

import br.uff.banco.shared.web.PaginaResponse;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Extrato de um período.
 * <p>
 * Os totais e os saldos se referem ao período inteiro, não só à página: vale
 * sempre {@code saldoInicial + totalCreditos - totalDebitos = saldoFinal}.
 *
 * @param de           primeiro dia do período (inclusive), no {@code fusoHorario}
 * @param ate          último dia do período (inclusive), no {@code fusoHorario}
 * @param saldoInicial saldo no início de {@code de}
 * @param saldoFinal   saldo no fim de {@code ate}
 */
public record ExtratoView(
        ContaResumo conta,
        LocalDate de,
        LocalDate ate,
        String fusoHorario,
        BigDecimal saldoInicial,
        BigDecimal totalCreditos,
        BigDecimal totalDebitos,
        BigDecimal saldoFinal,
        PaginaResponse<LancamentoView> lancamentos) {
}
