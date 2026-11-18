package br.uff.banco.contas.api;

import br.uff.banco.contas.application.AberturaContaService;
import br.uff.banco.contas.application.ContaView;
import br.uff.banco.contas.domain.TipoConta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin — contas")
@RestController
@RequestMapping("/api/admin/clientes/{clienteId}/contas")
class AdminContaController {

    private final AberturaContaService abertura;

    AdminContaController(AberturaContaService abertura) {
        this.abertura = abertura;
    }

    @Operation(summary = "Abre uma conta para o cliente", description = "O número é sequencial por agência e o dígito é calculado (módulo 11).")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ContaView abrir(@PathVariable Long clienteId, @Valid @RequestBody AberturaContaRequest requisicao) {
        return abertura.abrir(clienteId, requisicao.agenciaId(), requisicao.tipo());
    }

    @Operation(summary = "Lista as contas do cliente")
    @GetMapping
    List<ContaView> listar(@PathVariable Long clienteId) {
        return abertura.listarDoCliente(clienteId);
    }

    record AberturaContaRequest(@NotNull Long agenciaId, @NotNull TipoConta tipo) {
    }
}
