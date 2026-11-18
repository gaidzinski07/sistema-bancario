package br.uff.banco.contas.api;

import br.uff.banco.contas.application.ComprovanteOperacao;
import br.uff.banco.contas.application.ConsultaContaService;
import br.uff.banco.contas.application.ContaView;
import br.uff.banco.contas.application.ExtratoView;
import br.uff.banco.contas.application.OperacaoContaService;
import br.uff.banco.contas.domain.FormaDeposito;
import br.uff.banco.shared.domain.Dinheiro;
import br.uff.banco.shared.security.UsuarioAutenticado;
import br.uff.banco.shared.web.ValorMonetario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Contas do cliente autenticado. A conta nunca é de outra pessoa: o titular vem do token. */
@Tag(name = "Cliente — contas")
@RestController
@RequestMapping("/api/contas")
class ContaClienteController {

    private static final int DIAS_PADRAO_EXTRATO = 30;

    private final OperacaoContaService operacoes;
    private final ConsultaContaService consultas;
    private final Clock relogio;
    private final ZoneId fusoHorario;

    ContaClienteController(OperacaoContaService operacoes, ConsultaContaService consultas, Clock relogio, ZoneId fusoHorario) {
        this.operacoes = operacoes;
        this.consultas = consultas;
        this.relogio = relogio;
        this.fusoHorario = fusoHorario;
    }

    @Operation(summary = "Minhas contas, com saldo")
    @GetMapping
    List<ContaView> minhasContas(UsuarioAutenticado cliente) {
        return consultas.contasDoCliente(cliente.id());
    }

    @Operation(summary = "Detalhe de uma conta, com saldo")
    @GetMapping("/{contaId}")
    ContaView detalhar(UsuarioAutenticado cliente, @PathVariable Long contaId) {
        return consultas.detalhar(cliente.id(), contaId);
    }

    @Operation(summary = "Extrato por período",
            description = """
                    Datas no formato AAAA-MM-DD, no fuso do banco (America/Sao_Paulo), ambas inclusive.
                    Sem datas: últimos 30 dias até hoje. Período máximo: 366 dias.
                    Saldos e totais se referem ao período inteiro; os lançamentos vêm paginados, em ordem cronológica.""")
    @GetMapping("/{contaId}/extrato")
    ExtratoView extrato(
            UsuarioAutenticado cliente,
            @PathVariable Long contaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int tamanho) {
        LocalDate hoje = LocalDate.now(relogio.withZone(fusoHorario));
        LocalDate fimDoPeriodo = ate != null ? ate : hoje;
        LocalDate inicioDoPeriodo = de != null ? de : fimDoPeriodo.minusDays(DIAS_PADRAO_EXTRATO - 1);
        return consultas.extrato(cliente.id(), contaId, inicioDoPeriodo, fimDoPeriodo, pagina, tamanho);
    }

    @Operation(summary = "Depósito", description = "Exige a senha de transação.")
    @PostMapping("/{contaId}/depositos")
    @ResponseStatus(HttpStatus.CREATED)
    ComprovanteOperacao depositar(UsuarioAutenticado cliente, @PathVariable Long contaId,
            @Valid @RequestBody DepositoRequest requisicao) {
        return operacoes.depositar(cliente.id(), contaId, Dinheiro.of(requisicao.valor()), requisicao.forma(),
                requisicao.senhaTransacao());
    }

    @Operation(summary = "Saque", description = "Exige a senha de transação. Recusa se o saldo não for suficiente.")
    @PostMapping("/{contaId}/saques")
    @ResponseStatus(HttpStatus.CREATED)
    ComprovanteOperacao sacar(UsuarioAutenticado cliente, @PathVariable Long contaId,
            @Valid @RequestBody SaqueRequest requisicao) {
        return operacoes.sacar(cliente.id(), contaId, Dinheiro.of(requisicao.valor()), requisicao.senhaTransacao());
    }

    record DepositoRequest(
            @ValorMonetario BigDecimal valor,
            @NotNull FormaDeposito forma,
            @NotBlank @Size(max = 6) String senhaTransacao) {

        @Override
        public String toString() {
            return "DepositoRequest[valor=" + valor + ", forma=" + forma + "]";
        }
    }

    record SaqueRequest(
            @ValorMonetario BigDecimal valor,
            @NotBlank @Size(max = 6) String senhaTransacao) {

        @Override
        public String toString() {
            return "SaqueRequest[valor=" + valor + "]";
        }
    }
}
