package br.uff.banco;

import static br.uff.banco.suporte.ApiDeTeste.comToken;
import static br.uff.banco.suporte.ApiDeTeste.login;
import static br.uff.banco.suporte.ApiDeTeste.loginAdmin;
import static br.uff.banco.suporte.ApiDeTeste.loginCliente;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.uff.banco.contas.application.AberturaContaService;
import br.uff.banco.contas.application.ContaView;
import br.uff.banco.contas.domain.TipoConta;
import br.uff.banco.suporte.Cpfs;
import br.uff.banco.suporte.TesteDeIntegracao;
import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Cadastros administrativos e a fronteira de autorização entre ADMIN e CLIENTE. */
@TesteDeIntegracao
class AdministracaoIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    AberturaContaService aberturaConta;

    String tokenAdmin;

    @BeforeEach
    void autenticarAdmin() throws Exception {
        tokenAdmin = loginAdmin(mvc).accessToken();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/admin/clientes", "/api/admin/agencias", "/api/admin/clientes/1/contas"})
    void clienteRecebe403NasRotasDeAdmin(String rota) throws Exception {
        mvc.perform(comToken(get(rota), loginCliente(mvc).accessToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("acesso-negado"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/admin/clientes", "/api/admin/agencias"})
    void anonimoRecebe401NasRotasDeAdmin(String rota) throws Exception {
        mvc.perform(get(rota)).andExpect(status().isUnauthorized());
    }

    @Test
    void cadastraClienteQueConsegueLogarComoCliente() throws Exception {
        String cpf = Cpfs.novo();

        mvc.perform(comToken(post("/api/admin/clientes"), tokenAdmin).contentType(MediaType.APPLICATION_JSON).content("""
                        {
                          "nome": "Maria Teste",
                          "cpf": "%s",
                          "email": "maria.%s@banco.test",
                          "dataNascimento": "1990-03-01",
                          "senha": "SenhaForte1",
                          "segmento": "INVESTIDOR",
                          "endereco": {"logradouro": "Rua X", "cidade": "Niterói", "uf": "rj", "cep": "24020-000"}
                        }
                        """.formatted(cpf, cpf)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/api/admin/clientes/")))
                .andExpect(jsonPath("$.segmento").value("INVESTIDOR"))
                .andExpect(jsonPath("$.endereco.uf").value("RJ"))
                .andExpect(jsonPath("$.endereco.cep").value("24020000"))
                .andExpect(jsonPath("$.criadoEm").isNotEmpty());

        String tokenCliente = login(mvc, cpf, "SenhaForte1").accessToken();
        mvc.perform(comToken(get("/api/auth/me"), tokenCliente))
                .andExpect(jsonPath("$.papeis[0]").value("CLIENTE"));
    }

    @Test
    void recusaCpfDuplicado() throws Exception {
        String cpf = criarCliente().cpf();

        mvc.perform(comToken(post("/api/admin/clientes"), tokenAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoCliente(cpf)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("cpf-ja-cadastrado"));
    }

    @Test
    void validaCorpoDoCadastro() throws Exception {
        mvc.perform(comToken(post("/api/admin/clientes"), tokenAdmin).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "", "cpf": "529.982.247-25", "senha": "123"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("dados-invalidos"))
                .andExpect(jsonPath("$.erros[*].campo", org.hamcrest.Matchers.containsInAnyOrder("nome", "senha", "segmento")));
    }

    @Test
    void cpfComDigitoErradoViraRegraDeNegocio() throws Exception {
        mvc.perform(comToken(post("/api/admin/clientes"), tokenAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoCliente("52998224724")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("cpf-invalido"));
    }

    @Test
    void listaClientesPaginadoELimitaTamanho() throws Exception {
        mvc.perform(comToken(get("/api/admin/clientes").param("pagina", "0").param("tamanho", "2"), tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.length()").value(2))
                .andExpect(jsonPath("$.conteudo[0].id").value(1))
                .andExpect(jsonPath("$.tamanho").value(2));

        mvc.perform(comToken(get("/api/admin/clientes").param("tamanho", "500"), tokenAdmin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("dados-invalidos"));
    }

    @Test
    void clienteInexistenteDa404() throws Exception {
        mvc.perform(comToken(get("/api/admin/clientes/999999"), tokenAdmin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("nao-encontrado"));
    }

    @Test
    void criaAgenciaERecusaCodigoRepetido() throws Exception {
        String codigo = codigoAgenciaAleatorio();
        String corpo = """
                {"codigo": "%s", "nome": "Agência Teste"}
                """.formatted(codigo);

        mvc.perform(comToken(post("/api/admin/agencias"), tokenAdmin).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.nome").value("Agência Teste"));
        mvc.perform(comToken(post("/api/admin/agencias"), tokenAdmin).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("codigo-agencia-em-uso"));
    }

    @Test
    void abreContasComNumeracaoSequencialNaAgencia() throws Exception {
        long clienteId = criarCliente().id();
        long agenciaId = criarAgencia();

        abrirConta(clienteId, agenciaId, "CORRENTE")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value("00001"))
                .andExpect(jsonPath("$.digito").value("9"))
                .andExpect(jsonPath("$.saldo").value(0))
                .andExpect(jsonPath("$.status").value("ATIVA"));
        abrirConta(clienteId, agenciaId, "POUPANCA")
                .andExpect(jsonPath("$.numero").value("00002"))
                .andExpect(jsonPath("$.tipo").value("POUPANCA"));

        mvc.perform(comToken(get("/api/admin/clientes/{id}/contas", clienteId), tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void aberturasSimultaneasNaMesmaAgenciaNuncaRepetemNumero() throws Exception {
        long clienteId = criarCliente().id();
        long agenciaId = criarAgencia();
        int aberturas = 8;

        CountDownLatch largada = new CountDownLatch(1);
        List<Future<ContaView>> resultados = new ArrayList<>();
        try (ExecutorService threads = Executors.newFixedThreadPool(aberturas)) {
            for (int i = 0; i < aberturas; i++) {
                Callable<ContaView> abertura = () -> {
                    largada.await();
                    return aberturaConta.abrir(clienteId, agenciaId, TipoConta.CORRENTE);
                };
                resultados.add(threads.submit(abertura));
            }
            largada.countDown();
            List<String> numeros = new ArrayList<>();
            for (Future<ContaView> resultado : resultados) {
                numeros.add(resultado.get().numero());
            }
            assertThat(numeros).doesNotHaveDuplicates().hasSize(aberturas);
            assertThat(numeros).containsExactlyInAnyOrder("00001", "00002", "00003", "00004", "00005", "00006", "00007", "00008");
        }
    }

    @Test
    void naoAbreContaEmAgenciaInexistente() throws Exception {
        long clienteId = criarCliente().id();

        abrirConta(clienteId, 999999, "CORRENTE")
                .andExpect(status().isNotFound());
    }

    @Test
    void cadastraAdministrador() throws Exception {
        String cpf = Cpfs.novo();
        mvc.perform(comToken(post("/api/admin/administradores"), tokenAdmin).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "Novo Gerente", "cpf": "%s", "senha": "SenhaForte1", "perfil": "GERENTE"}
                        """.formatted(cpf)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.papeis[0]").value("ADMIN"))
                .andExpect(jsonPath("$.perfilAdministrador").value("GERENTE"));

        // o novo admin já acessa as rotas de admin
        mvc.perform(comToken(get("/api/admin/agencias"), login(mvc, cpf, "SenhaForte1").accessToken()))
                .andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions abrirConta(long clienteId, long agenciaId, String tipo) throws Exception {
        return mvc.perform(comToken(post("/api/admin/clientes/{id}/contas", clienteId), tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"agenciaId": %d, "tipo": "%s"}
                        """.formatted(agenciaId, tipo)));
    }

    private ClienteCriado criarCliente() throws Exception {
        String cpf = Cpfs.novo();
        String corpo = mvc.perform(comToken(post("/api/admin/clientes"), tokenAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoCliente(cpf)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return new ClienteCriado(cpf, ((Number) JsonPath.read(corpo, "$.id")).longValue());
    }

    private record ClienteCriado(String cpf, long id) {
    }

    private long criarAgencia() throws Exception {
        String corpo = mvc.perform(comToken(post("/api/admin/agencias"), tokenAdmin).contentType(MediaType.APPLICATION_JSON).content("""
                        {"codigo": "%s", "nome": "Agência de Teste"}
                        """.formatted(codigoAgenciaAleatorio())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(corpo, "$.id")).longValue();
    }

    /** Códigos 5000-9999 não colidem com o seed (0001-0003); colisão entre testes é improvável e daria 422 visível. */
    private static String codigoAgenciaAleatorio() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(5000, 10000));
    }

    private static String corpoCliente(String cpf) {
        return """
                {"nome": "Cliente Teste", "cpf": "%s", "senha": "SenhaForte1", "segmento": "CORRENTISTA"}
                """.formatted(cpf);
    }
}
