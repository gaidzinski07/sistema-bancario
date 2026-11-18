package br.uff.banco.investimentos.api;

import br.uff.banco.investimentos.application.FundoService;
import br.uff.banco.investimentos.application.Visoes.CotacaoView;
import br.uff.banco.investimentos.application.Visoes.FundoView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Consulta de fundos, para qualquer usuário autenticado. */
@Tag(name = "Fundos")
@RestController
@RequestMapping("/api/fundos")
class FundoController {

    private final FundoService fundos;
    private final Clock relogio;
    private final ZoneId fusoHorario;

    FundoController(FundoService fundos, Clock relogio, ZoneId fusoHorario) {
        this.fundos = fundos;
        this.relogio = relogio;
        this.fusoHorario = fusoHorario;
    }

    @Operation(summary = "Fundos disponíveis para aplicação")
    @GetMapping
    List<FundoView> ativos() {
        return fundos.ativos();
    }

    @Operation(summary = "Histórico da cota", description = "Sem datas: últimos 30 dias. Período máximo: 366 dias.")
    @GetMapping("/{fundoId}/cotacoes")
    List<CotacaoView> historico(
            @PathVariable Long fundoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        LocalDate fim = ate != null ? ate : LocalDate.now(relogio.withZone(fusoHorario));
        LocalDate inicio = de != null ? de : fim.minusDays(29);
        return fundos.historico(fundoId, inicio, fim);
    }
}
