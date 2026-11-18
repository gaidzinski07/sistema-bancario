package br.uff.banco.identidade.domain;

import br.uff.banco.shared.domain.Cpf;
import br.uff.banco.shared.domain.RegraNegocioException;
import br.uff.banco.shared.persistence.EntidadeBase;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Identidade de quem acessa o sistema: dados pessoais, credenciais e papéis.
 * <p>
 * Uma mesma pessoa pode ser cliente e administrador. Os dados de negócio do
 * cliente (segmento, endereço, contas) ficam em {@code clientes.domain.Cliente}.
 */
@Entity
@Table(name = "usuario")
public class Usuario extends EntidadeBase {

    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private Cpf cpf;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Column(name = "senha_transacao_hash", length = 100)
    private String senhaTransacaoHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_administrador", length = 20)
    private PerfilAdministrador perfilAdministrador;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @ElementCollection
    @CollectionTable(name = "usuario_papel", joinColumns = @JoinColumn(name = "usuario_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "papel", nullable = false, length = 20)
    private Set<Papel> papeis = new HashSet<>();

    protected Usuario() {
        // JPA
    }

    private Usuario(Cpf cpf, String nome, String email, LocalDate dataNascimento, String senhaHash) {
        this.cpf = Objects.requireNonNull(cpf, "cpf");
        this.nome = exigirTexto(nome, "nome");
        this.email = email == null || email.isBlank() ? null : email.trim().toLowerCase();
        this.dataNascimento = dataNascimento;
        this.senhaHash = exigirTexto(senhaHash, "senhaHash");
    }

    /** Cria um usuário sem papéis; conceda-os com {@link #tornarCliente()} / {@link #tornarAdministrador}. */
    public static Usuario novo(Cpf cpf, String nome, String email, LocalDate dataNascimento, String senhaHash) {
        return new Usuario(cpf, nome, email, dataNascimento, senhaHash);
    }

    public void tornarCliente() {
        papeis.add(Papel.CLIENTE);
    }

    public void tornarAdministrador(PerfilAdministrador perfil) {
        this.perfilAdministrador = Objects.requireNonNull(perfil, "perfil");
        papeis.add(Papel.ADMIN);
    }

    public boolean possuiPapel(Papel papel) {
        return papeis.contains(papel);
    }

    public void definirSenhaTransacao(String hash) {
        this.senhaTransacaoHash = exigirTexto(hash, "hash");
    }

    public void desativar() {
        this.ativo = false;
    }

    private static String exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("campo-obrigatorio", "O campo " + campo + " é obrigatório.");
        }
        return valor.trim();
    }

    public Cpf getCpf() {
        return cpf;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public String getSenhaTransacaoHash() {
        return senhaTransacaoHash;
    }

    public PerfilAdministrador getPerfilAdministrador() {
        return perfilAdministrador;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Set<Papel> getPapeis() {
        return papeis.isEmpty() ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(papeis));
    }
}
