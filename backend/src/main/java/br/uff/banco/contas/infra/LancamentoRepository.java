package br.uff.banco.contas.infra;

import br.uff.banco.contas.domain.Lancamento;
import br.uff.banco.contas.domain.TipoLancamento;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Lançamentos (ledger). As consultas do extrato usam o índice
 * {@code ix_lancamento_conta_data (conta_id, ocorrido_em)}.
 */
public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

    Optional<Lancamento> findByTipoAndOperacaoId(TipoLancamento tipo, Long operacaoId);

    /** Lançamentos de {@code [inicio, fim)}; a ordenação vem no {@code Pageable}. */
    @Query(value = "select l from Lancamento l where l.conta.id = :contaId and l.ocorridoEm >= :inicio and l.ocorridoEm < :fim",
            countQuery = "select count(l) from Lancamento l where l.conta.id = :contaId and l.ocorridoEm >= :inicio and l.ocorridoEm < :fim")
    Page<Lancamento> doPeriodo(@Param("contaId") Long contaId, @Param("inicio") Instant inicio, @Param("fim") Instant fim,
            Pageable paginacao);

    /**
     * Último lançamento antes de um instante. O {@code saldoApos} dele é o saldo da
     * conta naquele momento, sem precisar somar o histórico inteiro.
     */
    Optional<Lancamento> findFirstByContaIdAndOcorridoEmLessThanOrderByOcorridoEmDescIdDesc(Long contaId, Instant antesDe);

    /**
     * Total por natureza no período. SQL nativo porque {@code valor} é um
     * {@code Dinheiro} (AttributeConverter): o SUM tem de ser feito sobre o DECIMAL.
     *
     * @return linhas {@code [natureza (String), total (BigDecimal)]}
     */
    @Query(value = """
            SELECT natureza, COALESCE(SUM(valor), 0)
              FROM lancamento
             WHERE conta_id = :contaId AND ocorrido_em >= :inicio AND ocorrido_em < :fim
             GROUP BY natureza
            """, nativeQuery = true)
    List<Object[]> totaisPorNatureza(@Param("contaId") Long contaId, @Param("inicio") Instant inicio, @Param("fim") Instant fim);
}
