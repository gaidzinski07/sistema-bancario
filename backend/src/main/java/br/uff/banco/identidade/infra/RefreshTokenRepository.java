package br.uff.banco.identidade.infra;

import br.uff.banco.identidade.domain.RefreshToken;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Busca travando a linha: dois refresh simultâneos com o mesmo token não podem
     * os dois "vencer" a rotação.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RefreshToken t join fetch t.usuario where t.tokenHash = :hash")
    Optional<RefreshToken> findParaRotacao(@Param("hash") String hash);

    Optional<RefreshToken> findByTokenHash(String hash);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update RefreshToken t set t.revogadoEm = :agora where t.familia = :familia and t.revogadoEm is null")
    int revogarFamilia(@Param("familia") String familia, @Param("agora") Instant agora);
}
