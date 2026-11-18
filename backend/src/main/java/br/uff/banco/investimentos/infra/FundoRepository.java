package br.uff.banco.investimentos.infra;

import br.uff.banco.investimentos.domain.Fundo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundoRepository extends JpaRepository<Fundo, Long> {

    List<Fundo> findByAtivoTrueOrderByNome();

    List<Fundo> findAllByOrderByNome();

    boolean existsByNome(String nome);
}
