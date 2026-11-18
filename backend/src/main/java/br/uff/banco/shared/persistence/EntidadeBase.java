package br.uff.banco.shared.persistence;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/** Entidade mutável com ID surrogate gerado pelo banco (AUTO_INCREMENT). */
@MappedSuperclass
public abstract class EntidadeBase extends EntidadeAuditada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Override
    public Long getId() {
        return id;
    }
}
