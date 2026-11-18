package br.uff.banco.contas.application;

import br.uff.banco.contas.domain.Conta;
import br.uff.banco.contas.domain.Deposito;
import br.uff.banco.contas.domain.FormaDeposito;
import br.uff.banco.contas.domain.Lancamento;
import br.uff.banco.contas.domain.Saque;
import br.uff.banco.contas.infra.ContaRepository;
import br.uff.banco.contas.infra.DepositoRepository;
import br.uff.banco.contas.infra.LancamentoRepository;
import br.uff.banco.contas.infra.SaqueRepository;
import br.uff.banco.identidade.application.SenhaTransacaoService;
import br.uff.banco.shared.domain.Dinheiro;
import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Operações do cliente sobre uma conta própria: consulta, depósito e saque.
 * <p>
 * Cada operação: (1) confere a senha de transação, numa transação própria; (2) numa
 * segunda transação, trava a conta, registra a operação, movimenta o saldo e grava
 * o lançamento — qualquer falha desfaz o passo 2 inteiro (no legado eram commits
 * independentes por DAO).
 * <p>
 * Os dois passos são sequenciais, e não aninhados, de propósito: aninhados, cada
 * operação seguraria duas conexões do pool ao mesmo tempo, e sob carga todas
 * ficariam esperando a segunda conexão (deadlock de pool).
 */
@Service
public class OperacaoContaService {

    private final ContaRepository contas;
    private final ContasTravadas contasTravadas;
    private final DepositoRepository depositos;
    private final SaqueRepository saques;
    private final LancamentoRepository lancamentos;
    private final SenhaTransacaoService senhaTransacao;
    private final TransactionTemplate emTransacao;
    private final Clock relogio;

    OperacaoContaService(ContaRepository contas, ContasTravadas contasTravadas, DepositoRepository depositos,
            SaqueRepository saques, LancamentoRepository lancamentos, SenhaTransacaoService senhaTransacao,
            PlatformTransactionManager transacoes, Clock relogio) {
        this.contas = contas;
        this.contasTravadas = contasTravadas;
        this.depositos = depositos;
        this.saques = saques;
        this.lancamentos = lancamentos;
        this.senhaTransacao = senhaTransacao;
        this.emTransacao = new TransactionTemplate(transacoes);
        this.relogio = relogio;
    }

    @Transactional(readOnly = true)
    public List<ContaView> contasDoCliente(Long clienteId) {
        return contas.findByTitularIdOrderById(clienteId).stream().map(ContaView::de).toList();
    }

    /**
     * Depósito na conta do próprio cliente. Cheque é creditado na hora, como no
     * legado (a compensação é uma decisão de negócio em aberto).
     */
    public ComprovanteOperacao depositar(Long clienteId, Long contaId, Dinheiro valor, FormaDeposito forma, String senha) {
        senhaTransacao.verificar(clienteId, senha);
        return emTransacao.execute(status -> {
            Conta conta = contasTravadas.doTitular(contaId, clienteId);
            Deposito deposito = depositos.save(new Deposito(conta, forma, valor, relogio.instant()));
            Lancamento lancamento = lancamentos.save(conta.creditar(deposito.movimento()));
            return ComprovanteOperacao.de(lancamento);
        });
    }

    /** @throws br.uff.banco.contas.domain.SaldoInsuficienteException no legado o saque não conferia o saldo */
    public ComprovanteOperacao sacar(Long clienteId, Long contaId, Dinheiro valor, String senha) {
        senhaTransacao.verificar(clienteId, senha);
        return emTransacao.execute(status -> {
            Conta conta = contasTravadas.doTitular(contaId, clienteId);
            Saque saque = saques.save(new Saque(conta, valor, relogio.instant()));
            Lancamento lancamento = lancamentos.save(conta.debitar(saque.movimento()));
            return ComprovanteOperacao.de(lancamento);
        });
    }
}
