package br.uff.banco.contas.api;

import br.uff.banco.contas.application.ComprovanteTransferencia;
import br.uff.banco.contas.application.NovaTransferencia;
import br.uff.banco.contas.application.TransferenciaService;
import br.uff.banco.contas.application.TransferenciaService.ResultadoTransferencia;
import br.uff.banco.shared.domain.Dinheiro;
import br.uff.banco.shared.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cliente — transferências")
@RestController
@RequestMapping("/api/transferencias")
class TransferenciaController {

    private final TransferenciaService transferencias;

    TransferenciaController(TransferenciaService transferencias) {
        this.transferencias = transferencias;
    }

    @Operation(summary = "Transferência para outra conta do banco",
            description = """
                    Exige a senha de transação e o header Idempotency-Key (ex.: um UUID gerado pelo cliente).
                    Reenviar a mesma requisição com a mesma chave devolve o comprovante original (200) sem
                    transferir de novo. Primeira execução responde 201.""")
    @PostMapping
    ResponseEntity<ComprovanteTransferencia> transferir(
            UsuarioAutenticado cliente,
            @Parameter(description = "Chave única por transferência, gerada pelo cliente")
            @RequestHeader("Idempotency-Key") @NotBlank @Size(max = 64) String chaveIdempotencia,
            @Valid @RequestBody TransferenciaRequest requisicao) {
        ResultadoTransferencia resultado = transferencias.transferir(cliente.id(), new NovaTransferencia(
                chaveIdempotencia,
                requisicao.contaOrigemId(),
                requisicao.agenciaDestino(),
                requisicao.contaDestino(),
                requisicao.cpfDestino(),
                Dinheiro.of(requisicao.valor()),
                requisicao.senhaTransacao()));
        return ResponseEntity.status(resultado.nova() ? HttpStatus.CREATED : HttpStatus.OK).body(resultado.comprovante());
    }

    record TransferenciaRequest(
            @NotNull Long contaOrigemId,
            @NotBlank @Pattern(regexp = "\\d{4}", message = "deve ter 4 dígitos") String agenciaDestino,
            @NotBlank @Pattern(regexp = "\\d{1,10}-\\d", message = "deve estar no formato 00000-0") String contaDestino,
            @NotBlank @Size(max = 14) String cpfDestino,
            @ValorMonetario BigDecimal valor,
            @NotBlank @Size(max = 6) String senhaTransacao) {

        @Override
        public String toString() {
            return "TransferenciaRequest[origem=" + contaOrigemId + ", destino=" + agenciaDestino + "/" + contaDestino + ", valor=" + valor + "]";
        }
    }
}
