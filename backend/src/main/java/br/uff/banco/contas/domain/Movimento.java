package br.uff.banco.contas.domain;

import br.uff.banco.shared.domain.Dinheiro;
import java.time.Instant;
import java.util.Objects;

/**
 * Descreve uma movimentação de saldo: quanto, de que tipo e a qual operação
 * (depósito, saque, transferência...) ela pertence. Vira um {@link Lancamento}.
 */
public record Movimento(
        TipoLancamento tipo,
        Dinheiro valor,
        String descricao,
        String contraparte,
        Long operacaoId,
        Instant ocorridoEm) {

    public Movimento {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(valor, "valor").exigirPositivo();
        Objects.requireNonNull(descricao, "descricao");
        Objects.requireNonNull(ocorridoEm, "ocorridoEm");
    }
}
