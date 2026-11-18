package br.uff.banco.clientes.api;

import br.uff.banco.clientes.application.ClienteService;
import br.uff.banco.clientes.application.ClienteView;
import br.uff.banco.clientes.domain.SegmentoCliente;
import br.uff.banco.identidade.application.NovoUsuario;
import br.uff.banco.shared.web.EnderecoRequest;
import br.uff.banco.shared.web.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.time.LocalDate;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin — clientes")
@RestController
@RequestMapping("/api/admin/clientes")
class AdminClienteController {

    private final ClienteService clientes;

    AdminClienteController(ClienteService clientes) {
        this.clientes = clientes;
    }

    @Operation(summary = "Cadastra um cliente", description = "Cria o usuário (login por CPF + senha) e o cadastro de cliente.")
    @PostMapping
    ResponseEntity<ClienteView> cadastrar(@Valid @RequestBody NovoClienteRequest requisicao) {
        ClienteView criado = clientes.cadastrar(
                new NovoUsuario(requisicao.nome(), requisicao.cpf(), requisicao.email(), requisicao.dataNascimento(), requisicao.senha()),
                requisicao.segmento(),
                EnderecoRequest.paraDominio(requisicao.endereco()));
        return ResponseEntity.created(URI.create("/api/admin/clientes/" + criado.id())).body(criado);
    }

    @Operation(summary = "Lista os clientes (paginado, por ordem de cadastro)")
    @GetMapping
    PaginaResponse<ClienteView> listar(
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
        // Ordenação fixa: aceitar `sort` livre permitiria ordenar por colunas sensíveis (ex.: hash de senha).
        return PaginaResponse.de(clientes.listar(PageRequest.of(pagina, tamanho, Sort.by("id"))));
    }

    @Operation(summary = "Detalha um cliente")
    @GetMapping("/{id}")
    ClienteView buscar(@PathVariable Long id) {
        return clientes.buscar(id);
    }

    record NovoClienteRequest(
            @NotBlank @Size(max = 100) String nome,
            @NotBlank @Size(max = 14) String cpf,
            @Email @Size(max = 150) String email,
            @Past LocalDate dataNascimento,
            @NotBlank @Size(min = 8, max = 72) String senha,
            @NotNull SegmentoCliente segmento,
            @Valid EnderecoRequest endereco) {

        @Override
        public String toString() {
            return "NovoClienteRequest[nome=" + nome + "]";
        }
    }
}
