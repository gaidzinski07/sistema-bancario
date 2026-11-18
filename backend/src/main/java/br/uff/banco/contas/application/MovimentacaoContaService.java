package br.uff.banco.contas.application;

import br.uff.banco.contas.domain.Conta;
import br.uff.banco.contas.domain.Lancamento;
import br.uff.banco.contas.domain.Movimento;
import br.uff.banco.contas.infra.ContaRepository;
import br.uff.banco.contas.infra.LancamentoRepository;
import br.uff.banco.shared.domain.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Porta do módulo de contas para outros módulos (ex.: investimentos) que precisam
 * movimentar saldo. Mantém as mesmas garantias das operações do próprio módulo:
 * conta travada antes de mexer no saldo e lançamento gravado junto.
 * <p>
 * Todos os métodos exigem uma transação em andamento ({@code MANDATORY}): a
 * movimentação na conta e o registro do outro módulo precisam ser atômicos.
 */
@Service
public class MovimentacaoContaService {

    private final ContasTravadas contasTravadas;
    private final ContaRepository contas;
    private final LancamentoRepository lancamentos;

    MovimentacaoContaService(ContasTravadas contasTravadas, ContaRepository contas, LancamentoRepository lancamentos) {
        this.contasTravadas = contasTravadas;
        this.contas = contas;
        this.lancamentos = lancamentos;
    }

    /** Conta do cliente, travada para escrita. Conta de outra pessoa: 404. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Conta travarDoTitular(Long contaId, Long clienteId) {
        return contasTravadas.doTitular(contaId, clienteId);
    }

    /**
     * Conta dona de um registro do outro módulo, travada. Busca só o ID antes do lock
     * pela mesma razão de {@link ContaRepository#findParaAtualizacao}.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Conta travar(Long contaId) {
        return contas.findParaAtualizacao(contaId).orElseThrow(() -> RecursoNaoEncontradoException.de("Conta", contaId));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Lancamento debitar(Conta conta, Movimento movimento) {
        return lancamentos.save(conta.debitar(movimento));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Lancamento creditar(Conta conta, Movimento movimento) {
        return lancamentos.save(conta.creditar(movimento));
    }
}
