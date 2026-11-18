package br.uff.banco.clientes.infra;

import br.uff.banco.clientes.domain.Cliente;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** Traz o usuário no mesmo SELECT (evita N+1 na listagem). */
    @Override
    @EntityGraph(attributePaths = "usuario")
    Page<Cliente> findAll(Pageable paginacao);

    @EntityGraph(attributePaths = "usuario")
    Optional<Cliente> findComUsuarioById(Long id);
}
