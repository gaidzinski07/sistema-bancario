package br.uff.banco.contas.application;

import br.uff.banco.contas.domain.Conta;
import br.uff.banco.contas.domain.StatusConta;
import br.uff.banco.contas.domain.TipoConta;
import java.math.BigDecimal;

public record ContaView(
        Long id,
        String agencia,
        String numero,
        String digito,
        TipoConta tipo,
        BigDecimal saldo,
        StatusConta status,
        Long titularId) {

    public static ContaView de(Conta conta) {
        return new ContaView(
                conta.getId(),
                conta.getAgencia().getCodigo(),
                conta.getNumero(),
                conta.getDigito(),
                conta.getTipo(),
                conta.getSaldo().valor(),
                conta.getStatus(),
                conta.getTitular().getId());
    }
}
