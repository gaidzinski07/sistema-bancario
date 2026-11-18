package br.uff.banco.contas.infra;

import br.uff.banco.contas.domain.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositoRepository extends JpaRepository<Deposito, Long> {
}
