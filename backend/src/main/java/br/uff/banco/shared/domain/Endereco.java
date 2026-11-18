package br.uff.banco.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Endereço postal, embutido nas tabelas de cliente e agência.
 * <p>
 * No legado era um único {@code varchar(100)} ("Rua A, 1, Bairro A, Niterói - RJ").
 */
@Embeddable
public record Endereco(
        @Column(name = "logradouro") String logradouro,
        @Column(name = "numero") String numero,
        @Column(name = "complemento") String complemento,
        @Column(name = "bairro") String bairro,
        @Column(name = "cidade") String cidade,
        @Column(name = "uf") String uf,
        @Column(name = "cep") String cep) {

    public Endereco {
        if (uf != null) {
            uf = uf.toUpperCase();
        }
        if (cep != null) {
            cep = cep.replaceAll("\\D", "");
        }
    }
}
