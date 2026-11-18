package br.uff.banco.contas.infra;

import br.uff.banco.contas.domain.Saque;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaqueRepository extends JpaRepository<Saque, Long> {
}
