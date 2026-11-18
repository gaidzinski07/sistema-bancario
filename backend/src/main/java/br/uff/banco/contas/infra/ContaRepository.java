package br.uff.banco.contas.infra;

import br.uff.banco.contas.domain.Conta;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    @EntityGraph(attributePaths = "agencia")
    List<Conta> findByTitularIdOrderById(Long titularId);
}
