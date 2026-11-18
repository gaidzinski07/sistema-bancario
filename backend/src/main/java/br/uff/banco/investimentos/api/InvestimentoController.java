package br.uff.banco.investimentos.api;

import br.uff.banco.investimentos.application.InvestimentoService;
import br.uff.banco.investimentos.application.Visoes.ComprovanteInvestimento;
import br.uff.banco.investimentos.application.Visoes.DetalhePosicaoView;
import br.uff.banco.investimentos.application.Visoes.PosicaoView;
import br.uff.banco.shared.domain.Dinheiro;
import br.uff.banco.shared.security.UsuarioAutenticado;
import br.uff.banco.shared.web.ValorMonetario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Investimentos do cliente autenticado. */
@Tag(name = "Cliente — investimentos")
@RestController
@RequestMapping("/api/investimentos")
class InvestimentoController {

    private final InvestimentoService investimentos;

    InvestimentoController(InvestimentoService investimentos) {
        this.investimentos = investimentos;
    }

    @Operation(summary = "Aplica num fundo", description = "Debita a conta corrente informada. Exige a senha de transação.")
    @PostMapping("/aplicacoes")
    @ResponseStatus(HttpStatus.CREATED)
    ComprovanteInvestimento aplicar(UsuarioAutenticado cliente, @Valid @RequestBody AplicacaoRequest requisicao) {
        return investimentos.aplicar(cliente.id(), requisicao.contaId(), requisicao.fundoId(),
                Dinheiro.of(requisicao.valor()), requisicao.senhaTransacao());
    }

    @Operation(summary = "Carteira", description = "Posições com cotas, com valor atual e rendimento pela cota de agora.")
    @GetMapping("/posicoes")
    List<PosicaoView> carteira(UsuarioAutenticado cliente) {
        return investimentos.carteira(cliente.id());
    }

    @Operation(summary = "Detalhe de uma posição, com o histórico de aplicações e resgates")
    @GetMapping("/posicoes/{posicaoId}")
    DetalhePosicaoView detalhar(UsuarioAutenticado cliente, @PathVariable Long posicaoId) {
        return investimentos.detalhar(cliente.id(), posicaoId);
    }

    @Operation(summary = "Resgate parcial ou total",
            description = "Informe `valor` para resgate parcial ou `total: true` para resgatar tudo. Credita a conta corrente da posição.")
    @PostMapping("/posicoes/{posicaoId}/resgates")
    @ResponseStatus(HttpStatus.CREATED)
    ComprovanteInvestimento resgatar(UsuarioAutenticado cliente, @PathVariable Long posicaoId,
            @Valid @RequestBody ResgateRequest requisicao) {
        Dinheiro valor = Boolean.TRUE.equals(requisicao.total()) ? null : Dinheiro.of(requisicao.valor());
        return investimentos.resgatar(cliente.id(), posicaoId, valor, requisicao.senhaTransacao());
    }

    record AplicacaoRequest(
            @NotNull Long contaId,
            @NotNull Long fundoId,
            @ValorMonetario BigDecimal valor,
            @NotBlank @Size(max = 6) String senhaTransacao) {

        @Override
        public String toString() {
            return "AplicacaoRequest[conta=" + contaId + ", fundo=" + fundoId + ", valor=" + valor + "]";
        }
    }

    record ResgateRequest(
            @Positive @Digits(integer = 17, fraction = 2) BigDecimal valor,
            Boolean total,
            @NotBlank @Size(max = 6) String senhaTransacao) {

        /** Exatamente um dos dois: {@code valor} (parcial) ou {@code total: true}. */
        @AssertTrue(message = "informe o valor do resgate ou total: true, e não os dois")
        public boolean isValorOuTotal() {
            return (valor != null) != Boolean.TRUE.equals(total);
        }

        @Override
        public String toString() {
            return "ResgateRequest[valor=" + valor + ", total=" + total + "]";
        }
    }
}
