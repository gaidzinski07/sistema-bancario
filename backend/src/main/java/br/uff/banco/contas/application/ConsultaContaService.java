package br.uff.banco.contas.application;

import br.uff.banco.contas.domain.Conta;
import br.uff.banco.contas.domain.Lancamento;
import br.uff.banco.contas.domain.Natureza;
import br.uff.banco.contas.infra.ContaRepository;
import br.uff.banco.contas.infra.LancamentoRepository;
import br.uff.banco.shared.domain.RecursoNaoEncontradoException;
import br.uff.banco.shared.domain.RegraNegocioException;
import br.uff.banco.shared.web.PaginaResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consultas do cliente sobre as próprias contas: lista, detalhe e extrato.
 * <p>
 * No legado o saldo era uma tela com valores fixos (R$ 85,00) e o extrato era um
 * UNION de quatro tabelas, que mostrava o mesmo valor em "Entrada" e "Saída".
 * Aqui tudo sai do ledger ({@code lancamento}).
 */
@Service
public class ConsultaContaService {

    /** Limite do período de um extrato, para a consulta não varrer anos de histórico. */
    public static final long MAXIMO_DIAS_PERIODO = 366;

    private static final Sort ORDEM_DO_EXTRATO = Sort.by("ocorridoEm", "id");

    private final ContaRepository contas;
    private final LancamentoRepository lancamentos;
    private final ZoneId fusoHorario;

    ConsultaContaService(ContaRepository contas, LancamentoRepository lancamentos, ZoneId fusoHorario) {
        this.contas = contas;
        this.lancamentos = lancamentos;
        this.fusoHorario = fusoHorario;
    }

    @Transactional(readOnly = true)
    public List<ContaView> contasDoCliente(Long clienteId) {
        return contas.findByTitularIdOrderById(clienteId).stream().map(ContaView::de).toList();
    }

    @Transactional(readOnly = true)
    public ContaView detalhar(Long clienteId, Long contaId) {
        return ContaView.de(doTitular(clienteId, contaId));
    }

    /**
     * Extrato de {@code de} a {@code ate} (dias de calendário no fuso do banco, ambos inclusive).
     * <p>
     * Todas as consultas rodam na mesma transação de leitura: com o REPEATABLE READ do
     * InnoDB elas enxergam o mesmo snapshot, então saldos, totais e página batem
     * entre si mesmo com operações acontecendo ao mesmo tempo.
     */
    @Transactional(readOnly = true)
    public ExtratoView extrato(Long clienteId, Long contaId, LocalDate de, LocalDate ate, int pagina, int tamanho) {
        if (de.isAfter(ate)) {
            throw new RegraNegocioException("periodo-invalido", "A data inicial deve ser anterior ou igual à final.");
        }
        if (ChronoUnit.DAYS.between(de, ate) + 1 > MAXIMO_DIAS_PERIODO) {
            throw new RegraNegocioException("periodo-invalido",
                    "O período do extrato pode ter no máximo " + MAXIMO_DIAS_PERIODO + " dias.");
        }
        Conta conta = doTitular(clienteId, contaId);
        Instant inicio = de.atStartOfDay(fusoHorario).toInstant();
        Instant fim = ate.plusDays(1).atStartOfDay(fusoHorario).toInstant();

        BigDecimal saldoInicial = saldoAntesDe(contaId, inicio);
        BigDecimal saldoFinal = saldoAntesDe(contaId, fim);
        BigDecimal creditos = BigDecimal.ZERO;
        BigDecimal debitos = BigDecimal.ZERO;
        for (Object[] linha : lancamentos.totaisPorNatureza(contaId, inicio, fim)) {
            BigDecimal total = (BigDecimal) linha[1];
            if (Natureza.CREDITO.name().equals(linha[0])) {
                creditos = total;
            } else {
                debitos = total;
            }
        }
        var paginaDeLancamentos = lancamentos.doPeriodo(contaId, inicio, fim, PageRequest.of(pagina, tamanho, ORDEM_DO_EXTRATO));

        return new ExtratoView(
                ContaResumo.de(conta), de, ate, fusoHorario.getId(),
                saldoInicial, creditos, debitos, saldoFinal,
                PaginaResponse.de(paginaDeLancamentos, LancamentoView::de));
    }

    /** Saldo da conta num instante = saldo após o último lançamento anterior a ele. */
    private BigDecimal saldoAntesDe(Long contaId, Instant instante) {
        return lancamentos.findFirstByContaIdAndOcorridoEmLessThanOrderByOcorridoEmDescIdDesc(contaId, instante)
                .map(Lancamento::getSaldoApos)
                .map(saldo -> saldo.valor())
                .orElse(BigDecimal.ZERO.setScale(2));
    }

    /** Conta inexistente e conta de outra pessoa dão o mesmo 404. */
    private Conta doTitular(Long clienteId, Long contaId) {
        return contas.findById(contaId)
                .filter(conta -> conta.pertenceA(clienteId))
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Conta", contaId));
    }
}
