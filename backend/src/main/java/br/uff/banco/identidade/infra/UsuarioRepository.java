package br.uff.banco.identidade.infra;

import br.uff.banco.identidade.domain.Usuario;
import br.uff.banco.shared.domain.Cpf;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Carrega os papéis junto (são LAZY por padrão para não pesar nas listagens). */
    @EntityGraph(attributePaths = "papeis")
    Optional<Usuario> findByCpf(Cpf cpf);

    @EntityGraph(attributePaths = "papeis")
    Optional<Usuario> findComPapeisById(Long id);

    boolean existsByCpf(Cpf cpf);

    boolean existsByEmail(String email);
}
