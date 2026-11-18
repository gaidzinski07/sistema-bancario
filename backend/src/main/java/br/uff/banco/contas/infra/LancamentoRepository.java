package br.uff.banco.contas.infra;

import br.uff.banco.contas.domain.Lancamento;
import br.uff.banco.contas.domain.TipoLancamento;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

    Optional<Lancamento> findByTipoAndOperacaoId(TipoLancamento tipo, Long operacaoId);
}
