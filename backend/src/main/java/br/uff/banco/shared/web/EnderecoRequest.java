package br.uff.banco.shared.web;

import br.uff.banco.shared.domain.Endereco;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Endereço recebido pela API (cliente, agência). Todos os campos são opcionais. */
public record EnderecoRequest(
        @Size(max = 150) String logradouro,
        @Size(max = 10) String numero,
        @Size(max = 60) String complemento,
        @Size(max = 80) String bairro,
        @Size(max = 80) String cidade,
        @Pattern(regexp = "[A-Za-z]{2}", message = "deve ter 2 letras") String uf,
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "deve estar no formato 00000-000") String cep) {

    public Endereco paraDominio() {
        return new Endereco(logradouro, numero, complemento, bairro, cidade, uf, cep);
    }

    public static Endereco paraDominio(EnderecoRequest endereco) {
        return endereco == null ? null : endereco.paraDominio();
    }
}
