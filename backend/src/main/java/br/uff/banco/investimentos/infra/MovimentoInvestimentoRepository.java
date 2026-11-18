package br.uff.banco.investimentos.infra;

import br.uff.banco.investimentos.domain.MovimentoInvestimento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentoInvestimentoRepository extends JpaRepository<MovimentoInvestimento, Long> {

    List<MovimentoInvestimento> findByPosicaoIdOrderByOcorridoEmAscIdAsc(Long posicaoId);
}
