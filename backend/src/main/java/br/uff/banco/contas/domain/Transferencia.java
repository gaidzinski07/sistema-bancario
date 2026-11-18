package br.uff.banco.contas.domain;

import br.uff.banco.shared.domain.Dinheiro;
import br.uff.banco.shared.domain.RegraNegocioException;
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
 * Transferência entre contas do banco. Imutável.
 * <p>
 * A {@code chaveIdempotencia} (header {@code Idempotency-Key}) é única: se o cliente
 * reenviar a mesma requisição (timeout, duplo clique), a transferência não é feita
 * de novo.
 */
@Entity
@Table(name = "transferencia")
public class Transferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_origem_id", nullable = false)
    private Conta origem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_destino_id", nullable = false)
    private Conta destino;

    @Column(name = "valor", nullable = false, precision = 19, scale = 2)
    private Dinheiro valor;

    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 64)
    private String chaveIdempotencia;

    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    protected Transferencia() {
        // JPA
    }

    private Transferencia(Conta origem, Conta destino, Dinheiro valor, String chaveIdempotencia, Instant ocorridoEm) {
        this.origem = origem;
        this.destino = destino;
        this.valor = valor;
        this.chaveIdempotencia = chaveIdempotencia;
        this.ocorridoEm = ocorridoEm;
    }

    public static Transferencia entre(Conta origem, Conta destino, Dinheiro valor, String chaveIdempotencia, Instant agora) {
        Objects.requireNonNull(origem, "origem");
        Objects.requireNonNull(destino, "destino");
        Objects.requireNonNull(valor, "valor").exigirPositivo();
        if (origem.equals(destino)) {
            throw new RegraNegocioException("mesma-conta", "A conta de destino deve ser diferente da conta de origem.");
        }
        if (chaveIdempotencia == null || chaveIdempotencia.isBlank()) {
            throw new RegraNegocioException("idempotencia-obrigatoria", "A chave de idempotência é obrigatória.");
        }
        return new Transferencia(origem, destino, valor, chaveIdempotencia, Objects.requireNonNull(agora));
    }

    public Movimento movimentoDeSaida(String descricaoDestino) {
        return new Movimento(TipoLancamento.TRANSFERENCIA_ENVIADA, valor, "Transferência enviada", descricaoDestino, id, ocorridoEm);
    }

    public Movimento movimentoDeEntrada(String descricaoOrigem) {
        return new Movimento(TipoLancamento.TRANSFERENCIA_RECEBIDA, valor, "Transferência recebida", descricaoOrigem, id, ocorridoEm);
    }

    public Long getId() {
        return id;
    }

    public Conta getOrigem() {
        return origem;
    }

    public Conta getDestino() {
        return destino;
    }

    public Dinheiro getValor() {
        return valor;
    }

    public String getChaveIdempotencia() {
        return chaveIdempotencia;
    }

    public Instant getOcorridoEm() {
        return ocorridoEm;
    }
}
