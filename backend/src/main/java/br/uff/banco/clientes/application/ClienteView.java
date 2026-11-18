package br.uff.banco.clientes.application;

import br.uff.banco.clientes.domain.Cliente;
import br.uff.banco.clientes.domain.SegmentoCliente;
import br.uff.banco.shared.domain.Endereco;
import java.time.Instant;
import java.time.LocalDate;

public record ClienteView(
        Long id,
        String nome,
        String cpf,
        String email,
        LocalDate dataNascimento,
        SegmentoCliente segmento,
        Endereco endereco,
        boolean ativo,
        Instant criadoEm) {

    public static ClienteView de(Cliente cliente) {
        var usuario = cliente.getUsuario();
        return new ClienteView(
                cliente.getId(),
                usuario.getNome(),
                usuario.getCpf().formatado(),
                usuario.getEmail(),
                usuario.getDataNascimento(),
                cliente.getSegmento(),
                cliente.getEndereco(),
                usuario.isAtivo(),
                cliente.getCriadoEm());
    }
}
