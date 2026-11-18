package br.uff.banco.contas.infra;

import br.uff.banco.contas.domain.Transferencia;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {

    /** Para responder reenvios com a mesma {@code Idempotency-Key}: traz tudo que o comprovante usa. */
    @EntityGraph(attributePaths = {"origem.agencia", "destino.agencia", "destino.titular.usuario"})
    Optional<Transferencia> findByChaveIdempotencia(String chaveIdempotencia);
}
