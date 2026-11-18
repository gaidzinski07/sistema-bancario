package br.uff.banco.contas.api;

import br.uff.banco.contas.application.ComprovanteOperacao;
import br.uff.banco.contas.application.ContaView;
import br.uff.banco.contas.application.OperacaoContaService;
import br.uff.banco.contas.domain.FormaDeposito;
import br.uff.banco.shared.domain.Dinheiro;
import br.uff.banco.shared.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

/** Contas do cliente autenticado. A conta nunca é de outra pessoa: o titular vem do token. */
@Tag(name = "Cliente — contas")
@RestController
@RequestMapping("/api/contas")
class ContaClienteController {

    private final OperacaoContaService operacoes;

    ContaClienteController(OperacaoContaService operacoes) {
        this.operacoes = operacoes;
    }

    @Operation(summary = "Minhas contas, com saldo")
    @GetMapping
    List<ContaView> minhasContas(UsuarioAutenticado cliente) {
        return operacoes.contasDoCliente(cliente.id());
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
