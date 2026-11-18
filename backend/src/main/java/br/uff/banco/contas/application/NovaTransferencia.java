package br.uff.banco.contas.application;

import br.uff.banco.shared.domain.Dinheiro;

/**
 * Pedido de transferência.
 *
 * @param contaDestino número com dígito, ex.: {@code 00003-5}
 * @param cpfDestino   CPF do titular da conta de destino (no legado era lido e ignorado)
 */
public record NovaTransferencia(
        String chaveIdempotencia,
        Long contaOrigemId,
        String agenciaDestino,
        String contaDestino,
        String cpfDestino,
        Dinheiro valor,
        String senhaTransacao) {

    @Override
    public String toString() {
        return "NovaTransferencia[origem=" + contaOrigemId + ", destino=" + agenciaDestino + "/" + contaDestino + ", valor=" + valor + "]";
    }
}
