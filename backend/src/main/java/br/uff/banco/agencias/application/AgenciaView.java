package br.uff.banco.agencias.application;

import br.uff.banco.agencias.domain.Agencia;
import br.uff.banco.shared.domain.Endereco;

public record AgenciaView(Long id, String codigo, String nome, Endereco endereco) {

    public static AgenciaView de(Agencia agencia) {
        return new AgenciaView(agencia.getId(), agencia.getCodigo(), agencia.getNome(), agencia.getEndereco());
    }
}
