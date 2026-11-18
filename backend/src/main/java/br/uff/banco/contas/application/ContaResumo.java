package br.uff.banco.contas.application;

import br.uff.banco.contas.domain.Conta;

/** Identificação curta de uma conta em comprovantes e no extrato. */
public record ContaResumo(Long id, String agencia, String conta, String tipo) {

    static ContaResumo de(Conta conta) {
        return new ContaResumo(conta.getId(), conta.getAgencia().getCodigo(), conta.getNumeroComDigito(), conta.getTipo().name());
    }
}
