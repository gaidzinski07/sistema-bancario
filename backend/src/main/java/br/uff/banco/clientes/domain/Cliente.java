package br.uff.banco.clientes.domain;

import br.uff.banco.identidade.domain.Papel;
import br.uff.banco.identidade.domain.Usuario;
import br.uff.banco.shared.domain.Endereco;
import br.uff.banco.shared.domain.RegraNegocioException;
import br.uff.banco.shared.persistence.EntidadeAuditada;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Cliente do banco: o lado de negócio de um {@link Usuario} com papel CLIENTE.
 * <p>
 * Compartilha a chave primária com o usuário ({@code @MapsId}), como a tabela
 * CLIENTE do legado. A diferença é que agora a conta aponta para o titular — e
 * não o cliente para uma única conta —, então um cliente pode ter várias contas.
 */
@Entity
@Table(name = "cliente")
public class Cliente extends EntidadeAuditada {

    @Id
    @Column(name = "usuario_id")
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "segmento", nullable = false, length = 20)
    private SegmentoCliente segmento;

    @Embedded
    private Endereco endereco;

    protected Cliente() {
        // JPA
    }

    private Cliente(Usuario usuario, SegmentoCliente segmento, Endereco endereco) {
        this.usuario = Objects.requireNonNull(usuario, "usuario");
        this.segmento = Objects.requireNonNull(segmento, "segmento");
        this.endereco = endereco;
    }

    /** Torna o usuário cliente: concede o papel CLIENTE e cria o cadastro de negócio. */
    public static Cliente de(Usuario usuario, SegmentoCliente segmento, Endereco endereco) {
        if (usuario.possuiPapel(Papel.CLIENTE)) {
            throw new RegraNegocioException("ja-e-cliente", "Este usuário já é cliente.");
        }
        usuario.tornarCliente();
        return new Cliente(usuario, segmento, endereco);
    }

    public boolean isAtivo() {
        return usuario.isAtivo();
    }

    @Override
    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public SegmentoCliente getSegmento() {
        return segmento;
    }

    public Endereco getEndereco() {
        return endereco;
    }
}
