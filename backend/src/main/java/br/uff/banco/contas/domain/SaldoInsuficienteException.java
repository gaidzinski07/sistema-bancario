package br.uff.banco.contas.domain;

import br.uff.banco.shared.domain.RegraNegocioException;

public class SaldoInsuficienteException extends RegraNegocioException {

    public SaldoInsuficienteException() {
        super("saldo-insuficiente", "Saldo insuficiente para esta operação.");
    }
}
