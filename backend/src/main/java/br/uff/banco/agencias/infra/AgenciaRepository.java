package br.uff.banco.agencias.infra;

import br.uff.banco.agencias.domain.Agencia;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgenciaRepository extends JpaRepository<Agencia, Long> {

    boolean existsByCodigo(String codigo);

    /** {@code SELECT ... FOR UPDATE}: serializa a reserva de números de conta na agência. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Agencia a where a.id = :id")
    Optional<Agencia> findParaAtualizacao(@Param("id") Long id);
}
