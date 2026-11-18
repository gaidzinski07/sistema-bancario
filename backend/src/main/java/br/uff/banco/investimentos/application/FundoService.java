package br.uff.banco.investimentos.application;

import br.uff.banco.investimentos.application.Visoes.CotacaoView;
import br.uff.banco.investimentos.application.Visoes.FundoView;
import br.uff.banco.investimentos.domain.CotacaoFundo;
import br.uff.banco.investimentos.domain.Fundo;
import br.uff.banco.investimentos.infra.CotacaoFundoRepository;
import br.uff.banco.investimentos.infra.FundoRepository;
import br.uff.banco.shared.domain.Dinheiro;
import br.uff.banco.shared.domain.RecursoNaoEncontradoException;
import br.uff.banco.shared.domain.RegraNegocioException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fundos e cotações.
 * <p>
 * Decisão 8: a cota é informada pelo administrador. Cada atualização grava a cota
 * do dia no histórico ({@code cotacao_fundo}, uma linha por dia; a última do dia vale)
 * e passa a valer imediatamente para aplicações, resgates e carteira.
 */
@Service
public class FundoService {

    private static final long MAXIMO_DIAS_HISTORICO = 366;

    private final FundoRepository fundos;
    private final CotacaoFundoRepository cotacoes;
    private final Clock relogio;
    private final ZoneId fusoHorario;

    FundoService(FundoRepository fundos, CotacaoFundoRepository cotacoes, Clock relogio, ZoneId fusoHorario) {
        this.fundos = fundos;
        this.cotacoes = cotacoes;
        this.relogio = relogio;
        this.fusoHorario = fusoHorario;
    }

    @Transactional(readOnly = true)
    public List<FundoView> ativos() {
        return fundos.findByAtivoTrueOrderByNome().stream().map(FundoView::de).toList();
    }

    @Transactional(readOnly = true)
    public List<FundoView> todos() {
        return fundos.findAllByOrderByNome().stream().map(FundoView::de).toList();
    }

    @Transactional
    public FundoView criar(String nome, BigDecimal cotaInicial, Dinheiro aplicacaoMinima) {
        if (nome != null && fundos.existsByNome(nome.trim())) {
            throw new RegraNegocioException("fundo-ja-existe", "Já existe um fundo com o nome " + nome.trim() + ".");
        }
        Fundo fundo = fundos.save(new Fundo(nome, cotaInicial, aplicacaoMinima));
        cotacoes.save(new CotacaoFundo(fundo, hoje(), fundo.getCotaAtual()));
        return FundoView.de(fundo);
    }

    @Transactional
    public FundoView atualizarCota(Long fundoId, BigDecimal novaCota) {
        Fundo fundo = obter(fundoId);
        fundo.atualizarCota(novaCota);
        LocalDate hoje = hoje();
        cotacoes.findByFundoIdAndData(fundoId, hoje)
                .ifPresentOrElse(
                        cotacao -> cotacao.corrigir(fundo.getCotaAtual()),
                        () -> cotacoes.save(new CotacaoFundo(fundo, hoje, fundo.getCotaAtual())));
        return FundoView.de(fundo);
    }

    /** Fundo desativado não aceita aplicações; quem já tem posição continua podendo resgatar. */
    @Transactional
    public FundoView desativar(Long fundoId) {
        Fundo fundo = obter(fundoId);
        fundo.desativar();
        return FundoView.de(fundo);
    }

    @Transactional(readOnly = true)
    public List<CotacaoView> historico(Long fundoId, LocalDate de, LocalDate ate) {
        obter(fundoId);
        if (de.isAfter(ate) || ChronoUnit.DAYS.between(de, ate) + 1 > MAXIMO_DIAS_HISTORICO) {
            throw new RegraNegocioException("periodo-invalido",
                    "Período inválido: a data inicial deve ser anterior à final e o período ter até " + MAXIMO_DIAS_HISTORICO + " dias.");
        }
        return cotacoes.findByFundoIdAndDataBetweenOrderByData(fundoId, de, ate).stream().map(CotacaoView::de).toList();
    }

    private Fundo obter(Long fundoId) {
        return fundos.findById(fundoId).orElseThrow(() -> RecursoNaoEncontradoException.de("Fundo", fundoId));
    }

    private LocalDate hoje() {
        return LocalDate.now(relogio.withZone(fusoHorario));
    }
}
