package br.uff.banco.investimentos.api;

import br.uff.banco.investimentos.application.FundoService;
import br.uff.banco.investimentos.application.Visoes.FundoView;
import br.uff.banco.shared.domain.Dinheiro;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin — fundos")
@RestController
@RequestMapping("/api/admin/fundos")
class AdminFundoController {

    private final FundoService fundos;

    AdminFundoController(FundoService fundos) {
        this.fundos = fundos;
    }

    @Operation(summary = "Lista todos os fundos, inclusive inativos")
    @GetMapping
    List<FundoView> listar() {
        return fundos.todos();
    }

    @Operation(summary = "Cadastra um fundo", description = "A cota inicial vira a primeira cotação do histórico.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    FundoView criar(@Valid @RequestBody NovoFundoRequest requisicao) {
        return fundos.criar(requisicao.nome(), requisicao.cotaInicial(), Dinheiro.of(requisicao.aplicacaoMinima()));
    }

    @Operation(summary = "Atualiza a cota do dia",
            description = "Vale imediatamente para aplicações, resgates e carteira. Mais de uma atualização no mesmo dia corrige a cotação do dia.")
    @PutMapping("/{fundoId}/cotacao")
    FundoView atualizarCota(@PathVariable Long fundoId, @Valid @RequestBody CotacaoRequest requisicao) {
        return fundos.atualizarCota(fundoId, requisicao.valorCota());
    }

    @Operation(summary = "Desativa o fundo", description = "Bloqueia novas aplicações; quem tem posição continua podendo resgatar.")
    @DeleteMapping("/{fundoId}")
    FundoView desativar(@PathVariable Long fundoId) {
        return fundos.desativar(fundoId);
    }

    record NovoFundoRequest(
            @NotBlank @Size(max = 100) String nome,
            @NotNull @Positive @Digits(integer = 11, fraction = 8) BigDecimal cotaInicial,
            @NotNull @PositiveOrZero @Digits(integer = 17, fraction = 2) BigDecimal aplicacaoMinima) {
    }

    record CotacaoRequest(@NotNull @Positive @Digits(integer = 11, fraction = 8) BigDecimal valorCota) {
    }
}
