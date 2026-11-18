package br.uff.banco.shared.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Base das entidades mutáveis: datas de auditoria e igualdade por ID.
 * <p>
 * Não define o ID: a maioria das entidades usa {@link EntidadeBase} (ID gerado),
 * mas {@code Cliente} compartilha a PK do {@code Usuario} via {@code @MapsId}.
 * <p>
 * Operações imutáveis (depósito, saque, lançamento) não herdam daqui: elas têm só
 * {@code ocorrido_em}, sem {@code atualizado_em}.
 */
@MappedSuperclass
public abstract class EntidadeAuditada {

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    public abstract Long getId();

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

    /**
     * Igualdade por ID, segura para proxies do Hibernate: usa {@code getId()} (que o
     * proxy responde sem inicializar) e compara a classe real. Entidades ainda não
     * persistidas (ID nulo) só são iguais a si mesmas.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EntidadeAuditada outra) || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        return getId() != null && getId().equals(outra.getId());
    }

    @Override
    public int hashCode() {
        // Constante por classe: o hash não muda quando o ID é atribuído no persist.
        return Hibernate.getClass(this).hashCode();
    }
}
