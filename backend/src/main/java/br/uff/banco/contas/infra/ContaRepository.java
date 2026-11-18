package br.uff.banco.contas.infra;

import br.uff.banco.contas.domain.Conta;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    @EntityGraph(attributePaths = "agencia")
    List<Conta> findByTitularIdOrderById(Long titularId);

    /**
     * {@code SELECT ... FOR UPDATE} só na linha da conta (sem join fetch: no MySQL o
     * lock se estenderia às linhas de agência/cliente/usuário do join).
     * <p>
     * A conta não pode estar carregada antes na mesma transação: o Hibernate
     * devolveria a instância já em memória, com o saldo lido antes do lock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conta c where c.id = :id")
    Optional<Conta> findParaAtualizacao(@Param("id") Long id);

    /** Só o ID, de propósito: a entidade é carregada depois, já travada (ver {@link #findParaAtualizacao}). */
    @Query("select c.id from Conta c where c.agencia.codigo = :agencia and c.numero = :numero")
    Optional<Long> findIdPorAgenciaENumero(@Param("agencia") String agencia, @Param("numero") String numero);
}
