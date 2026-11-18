package br.uff.banco.agencias.domain;

import br.uff.banco.shared.domain.Endereco;
import br.uff.banco.shared.domain.RegraNegocioException;
import br.uff.banco.shared.persistence.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Agência bancária.
 * <p>
 * Controla a numeração das contas abertas nela ({@code proximoNumeroConta}). A
 * reserva de número é feita com a linha da agência travada
 * ({@code SELECT ... FOR UPDATE}), então duas aberturas simultâneas nunca recebem
 * o mesmo número.
 */
@Entity
@Table(name = "agencia")
public class Agencia extends EntidadeBase {

    @Column(name = "codigo", nullable = false, unique = true, length = 4)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Embedded
    private Endereco endereco;

    @Column(name = "proximo_numero_conta", nullable = false)
    private long proximoNumeroConta = 1;

    protected Agencia() {
        // JPA
    }

    public Agencia(String codigo, String nome, Endereco endereco) {
        if (codigo == null || !codigo.matches("\\d{4}")) {
            throw new RegraNegocioException("codigo-agencia-invalido", "O código da agência deve ter 4 dígitos.");
        }
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("campo-obrigatorio", "O nome da agência é obrigatório.");
        }
        this.codigo = codigo;
        this.nome = nome.trim();
        this.endereco = endereco;
    }

    /** Reserva o próximo número de conta desta agência. Chamar com a agência travada para escrita. */
    public long reservarNumeroConta() {
        return proximoNumeroConta++;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public Endereco getEndereco() {
        return endereco;
    }
}
