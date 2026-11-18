package br.uff.banco.investimentos.infra;

import br.uff.banco.investimentos.domain.CotacaoFundo;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CotacaoFundoRepository extends JpaRepository<CotacaoFundo, Long> {

    Optional<CotacaoFundo> findByFundoIdAndData(Long fundoId, LocalDate data);

    List<CotacaoFundo> findByFundoIdAndDataBetweenOrderByData(Long fundoId, LocalDate de, LocalDate ate);
}
