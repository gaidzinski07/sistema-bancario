package br.uff.banco.identidade.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

/**
 * Refresh token opaco, guardado apenas como hash SHA-256.
 * <p>
 * Rotação: cada uso revoga o token e emite outro da mesma {@code familia}. Se um
 * token já revogado for apresentado de novo, é sinal de que vazou — a família
 * inteira é revogada e o usuário precisa logar novamente.
 */
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "familia", nullable = false, length = 36)
    private String familia;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "revogado_em")
    private Instant revogadoEm;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected RefreshToken() {
        // JPA
    }

    public RefreshToken(Usuario usuario, String tokenHash, String familia, Instant criadoEm, Instant expiraEm) {
        this.usuario = Objects.requireNonNull(usuario);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.familia = Objects.requireNonNull(familia);
        this.criadoEm = Objects.requireNonNull(criadoEm);
        this.expiraEm = Objects.requireNonNull(expiraEm);
    }

    public boolean isRevogado() {
        return revogadoEm != null;
    }

    public boolean isExpirado(Instant agora) {
        return !agora.isBefore(expiraEm);
    }

    public void revogar(Instant agora) {
        if (revogadoEm == null) {
            revogadoEm = agora;
        }
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getFamilia() {
        return familia;
    }

    public Instant getExpiraEm() {
        return expiraEm;
    }
}
