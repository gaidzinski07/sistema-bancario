package br.uff.banco.investimentos.infra;

import br.uff.banco.investimentos.domain.Posicao;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Posições.
 * <p>
 * Ordem de locks: sempre a conta dona primeiro, depois a posição. As leituras de
 * posição para alterar são <em>locking reads</em> ({@code FOR UPDATE}): no
 * REPEATABLE READ do InnoDB, uma leitura comum pode vir do snapshot do início da
 * transação e não enxergar uma aplicação/resgate que acabou de ser commitado.
 */
public interface PosicaoRepository extends JpaRepository<Posicao, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Posicao p where p.conta.id = :contaId and p.fundo.id = :fundoId")
    Optional<Posicao> findParaAtualizacao(@Param("contaId") Long contaId, @Param("fundoId") Long fundoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Posicao p where p.id = :id")
    Optional<Posicao> findParaAtualizacao(@Param("id") Long id);

    /** Só o ID da conta dona, para travar a conta antes de carregar a posição. */
    @Query("select p.conta.id from Posicao p where p.id = :id")
    Optional<Long> findContaIdById(@Param("id") Long id);

    /** Carteira do cliente: posições com cotas, com fundo e agência no mesmo SELECT. */
    @EntityGraph(attributePaths = {"fundo", "conta.agencia"})
    @Query("select p from Posicao p where p.conta.titular.id = :clienteId and p.quantidadeCotas > 0 order by p.id")
    List<Posicao> carteiraDoCliente(@Param("clienteId") Long clienteId);

    @EntityGraph(attributePaths = {"fundo", "conta.agencia"})
    @Query("select p from Posicao p where p.id = :id")
    Optional<Posicao> findDetalhada(@Param("id") Long id);
}
