package br.uff.banco.contas.application;

import br.uff.banco.contas.domain.Conta;
import br.uff.banco.contas.infra.ContaRepository;
import br.uff.banco.shared.domain.RecursoNaoEncontradoException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carrega contas com lock de escrita, para as operações que mexem no saldo.
 * <p>
 * Regra de ordem: quando duas contas são travadas na mesma transação, a de menor ID
 * vem primeiro. Assim, A→B e B→A simultâneas esperam uma pela outra em vez de
 * entrar em deadlock.
 */
@Component
class ContasTravadas {

    private final ContaRepository contas;

    ContasTravadas(ContaRepository contas) {
        this.contas = contas;
    }

    /**
     * Conta do cliente, travada. Conta inexistente e conta de outra pessoa dão o
     * mesmo 404 — no legado as operações usavam a conta 1 para qualquer usuário.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    Conta doTitular(Long contaId, Long clienteId) {
        return contas.findParaAtualizacao(contaId)
                .filter(conta -> conta.pertenceA(clienteId))
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Conta", contaId));
    }

    /** Trava as duas contas em ordem de ID e devolve na ordem pedida (origem, destino). */
    @Transactional(propagation = Propagation.MANDATORY)
    Par par(Long origemId, Long destinoId) {
        Long primeiro = Math.min(origemId, destinoId);
        Long segundo = Math.max(origemId, destinoId);
        Conta contaPrimeira = travar(primeiro);
        Conta contaSegunda = primeiro.equals(segundo) ? contaPrimeira : travar(segundo);
        return origemId.equals(primeiro) ? new Par(contaPrimeira, contaSegunda) : new Par(contaSegunda, contaPrimeira);
    }

    private Conta travar(Long id) {
        return contas.findParaAtualizacao(id).orElseThrow(() -> RecursoNaoEncontradoException.de("Conta", id));
    }

    record Par(Conta origem, Conta destino) {
    }
}
