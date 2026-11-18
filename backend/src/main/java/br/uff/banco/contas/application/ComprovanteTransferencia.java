package br.uff.banco.contas.application;

import br.uff.banco.contas.application.ComprovanteOperacao.ContaResumo;
import br.uff.banco.contas.domain.Transferencia;
import br.uff.banco.shared.domain.Dinheiro;
import java.math.BigDecimal;
import java.time.Instant;

public record ComprovanteTransferencia(
        Long id,
        BigDecimal valor,
        Instant ocorridoEm,
        ContaResumo origem,
        ContaResumo destino,
        String favorecido,
        BigDecimal saldoOrigemApos) {

    static ComprovanteTransferencia de(Transferencia transferencia, Dinheiro saldoOrigemApos) {
        return new ComprovanteTransferencia(
                transferencia.getId(),
                transferencia.getValor().valor(),
                transferencia.getOcorridoEm(),
                ContaResumo.de(transferencia.getOrigem()),
                ContaResumo.de(transferencia.getDestino()),
                transferencia.getDestino().getTitular().getUsuario().getNome(),
                saldoOrigemApos.valor());
    }
}
