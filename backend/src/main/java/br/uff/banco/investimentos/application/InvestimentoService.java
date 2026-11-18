package br.uff.banco.investimentos.application;

import br.uff.banco.contas.application.ContaResumo;
import br.uff.banco.contas.application.MovimentacaoContaService;
import br.uff.banco.contas.domain.Conta;
import br.uff.banco.contas.domain.Lancamento;
import br.uff.banco.contas.domain.Movimento;
import br.uff.banco.contas.domain.TipoLancamento;
import br.uff.banco.identidade.application.SenhaTransacaoService;
import br.uff.banco.investimentos.application.Visoes.ComprovanteInvestimento;
import br.uff.banco.investimentos.application.Visoes.DetalhePosicaoView;
import br.uff.banco.investimentos.application.Visoes.FundoResumo;
import br.uff.banco.investimentos.application.Visoes.MovimentoView;
import br.uff.banco.investimentos.application.Visoes.PosicaoView;
import br.uff.banco.investimentos.domain.Fundo;
import br.uff.banco.investimentos.domain.MovimentoInvestimento;
import br.uff.banco.investimentos.domain.Posicao;
import br.uff.banco.investimentos.infra.FundoRepository;
import br.uff.banco.investimentos.infra.MovimentoInvestimentoRepository;
import br.uff.banco.investimentos.infra.PosicaoRepository;
import br.uff.banco.shared.domain.Dinheiro;
import br.uff.banco.shared.domain.RecursoNaoEncontradoException;
import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Aplicação, resgate e carteira do cliente.
 * <p>
 * Mesmo desenho das operações de conta: a senha de transação é conferida antes,
 * em transação própria; depois, numa única transação, a conta é travada, a posição
 * é travada (ou criada), o movimento é registrado e o saldo da conta corrente
 * muda com o lançamento correspondente.
 * <p>
 * A "transferência a partir do investimento" do legado (que creditava o destino sem
 * debitar nada) não existe mais (decisão 3): o caminho é resgate → conta → transferência.
 */
@Service
public class InvestimentoService {

    private final MovimentacaoContaService contas;
    private final FundoRepository fundos;
    private final PosicaoRepository posicoes;
    private final MovimentoInvestimentoRepository movimentos;
    private final SenhaTransacaoService senhaTransacao;
    private final TransactionTemplate emTransacao;
    private final Clock relogio;

    InvestimentoService(MovimentacaoContaService contas, FundoRepository fundos, PosicaoRepository posicoes,
            MovimentoInvestimentoRepository movimentos, SenhaTransacaoService senhaTransacao,
            PlatformTransactionManager transacoes, Clock relogio) {
        this.contas = contas;
        this.fundos = fundos;
        this.posicoes = posicoes;
        this.movimentos = movimentos;
        this.senhaTransacao = senhaTransacao;
        this.emTransacao = new TransactionTemplate(transacoes);
        this.relogio = relogio;
    }

    /** Aplica a partir da conta corrente do cliente. Abre a posição no primeiro uso do fundo. */
    public ComprovanteInvestimento aplicar(Long clienteId, Long contaId, Long fundoId, Dinheiro valor, String senha) {
        senhaTransacao.verificar(clienteId, senha);
        return emTransacao.execute(status -> {
            // Conta travada antes de qualquer outra leitura (ordem de locks: conta → posição).
            Conta conta = contas.travarDoTitular(contaId, clienteId);
            Fundo fundo = fundos.findById(fundoId).orElseThrow(() -> RecursoNaoEncontradoException.de("Fundo", fundoId));
            Posicao posicao = posicoes.findParaAtualizacao(contaId, fundoId)
                    .orElseGet(() -> posicoes.save(Posicao.abrir(conta, fundo)));

            MovimentoInvestimento movimento = movimentos.save(posicao.aplicar(valor, relogio.instant()));
            Lancamento lancamento = contas.debitar(conta, new Movimento(TipoLancamento.APLICACAO, movimento.getValor(),
                    "Aplicação em fundo", fundo.getNome(), movimento.getId(), movimento.getOcorridoEm()));
            return comprovante(movimento, lancamento);
        });
    }

    /**
     * Resgate para a conta corrente da posição (decisão 4).
     *
     * @param valor valor a resgatar; {@code null} = resgate total
     */
    public ComprovanteInvestimento resgatar(Long clienteId, Long posicaoId, Dinheiro valor, String senha) {
        senhaTransacao.verificar(clienteId, senha);
        return emTransacao.execute(status -> {
            Long contaId = posicoes.findContaIdById(posicaoId)
                    .orElseThrow(() -> RecursoNaoEncontradoException.de("Posição", posicaoId));
            // Posição de outro cliente dá o mesmo 404 de posição inexistente.
            Conta conta = contas.travarDoTitular(contaId, clienteId);
            Posicao posicao = posicoes.findParaAtualizacao(posicaoId)
                    .orElseThrow(() -> RecursoNaoEncontradoException.de("Posição", posicaoId));

            MovimentoInvestimento movimento = movimentos.save(valor == null
                    ? posicao.resgatarTudo(relogio.instant())
                    : posicao.resgatar(valor, relogio.instant()));
            Lancamento lancamento = contas.creditar(conta, new Movimento(TipoLancamento.RESGATE, movimento.getValor(),
                    "Resgate de fundo", posicao.getFundo().getNome(), movimento.getId(), movimento.getOcorridoEm()));
            return comprovante(movimento, lancamento);
        });
    }

    /** Posições com cotas, com valor atual e rendimento pela cota de agora. */
    @Transactional(readOnly = true)
    public List<PosicaoView> carteira(Long clienteId) {
        return posicoes.carteiraDoCliente(clienteId).stream().map(PosicaoView::de).toList();
    }

    @Transactional(readOnly = true)
    public DetalhePosicaoView detalhar(Long clienteId, Long posicaoId) {
        Posicao posicao = posicoes.findDetalhada(posicaoId)
                .filter(p -> p.pertenceA(clienteId))
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Posição", posicaoId));
        return new DetalhePosicaoView(PosicaoView.de(posicao),
                movimentos.findByPosicaoIdOrderByOcorridoEmAscIdAsc(posicaoId).stream().map(MovimentoView::de).toList());
    }

    private static ComprovanteInvestimento comprovante(MovimentoInvestimento movimento, Lancamento lancamento) {
        Posicao posicao = movimento.getPosicao();
        return new ComprovanteInvestimento(
                MovimentoView.de(movimento),
                FundoResumo.de(posicao.getFundo()),
                ContaResumo.de(lancamento.getConta()),
                lancamento.getSaldoApos().valor(),
                PosicaoView.de(posicao));
    }
}
