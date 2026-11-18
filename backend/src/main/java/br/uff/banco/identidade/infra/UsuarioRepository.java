package br.uff.banco.identidade.infra;

import br.uff.banco.identidade.domain.Usuario;
import br.uff.banco.shared.domain.Cpf;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Carrega os papéis junto (são LAZY por padrão para não pesar nas listagens). */
    @EntityGraph(attributePaths = "papeis")
    Optional<Usuario> findByCpf(Cpf cpf);

    @EntityGraph(attributePaths = "papeis")
    Optional<Usuario> findComPapeisById(Long id);

    /** Trava a linha do usuário: tentativas simultâneas de senha de transação contam uma a uma. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> findParaAtualizacao(@Param("id") Long id);

    boolean existsByCpf(Cpf cpf);

    boolean existsByEmail(String email);
}
