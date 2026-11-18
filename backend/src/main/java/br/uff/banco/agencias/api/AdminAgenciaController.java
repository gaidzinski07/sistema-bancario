package br.uff.banco.agencias.api;

import br.uff.banco.agencias.application.AgenciaService;
import br.uff.banco.agencias.application.AgenciaView;
import br.uff.banco.shared.web.EnderecoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin — agências")
@RestController
@RequestMapping("/api/admin/agencias")
class AdminAgenciaController {

    private final AgenciaService agencias;

    AdminAgenciaController(AgenciaService agencias) {
        this.agencias = agencias;
    }

    @Operation(summary = "Lista as agências")
    @GetMapping
    List<AgenciaView> listar() {
        return agencias.listar();
    }

    @Operation(summary = "Cadastra uma agência")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AgenciaView criar(@Valid @RequestBody NovaAgenciaRequest requisicao) {
        return agencias.criar(requisicao.codigo(), requisicao.nome(), EnderecoRequest.paraDominio(requisicao.endereco()));
    }

    record NovaAgenciaRequest(
            @NotBlank @Pattern(regexp = "\\d{4}", message = "deve ter 4 dígitos") String codigo,
            @NotBlank @Size(max = 100) String nome,
            @Valid EnderecoRequest endereco) {
    }
}
