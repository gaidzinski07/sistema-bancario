package br.uff.banco.suporte;

import static br.uff.banco.suporte.ApiDeTeste.comToken;
import static br.uff.banco.suporte.ApiDeTeste.login;
import static br.uff.banco.suporte.ApiDeTeste.loginAdmin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Monta cenários pela própria API (sem atalhos no banco): cliente novo, com conta
 * corrente e poupança na agência 0001 e senha de transação definida.
 */
public final class Cenarios {

    public static final String SENHA_LOGIN = "SenhaForte1";
    public static final String SENHA_TRANSACAO = "4321";

    private final MockMvc mvc;

    public Cenarios(MockMvc mvc) {
        this.mvc = mvc;
    }

    public ClienteDeTeste clienteComContas() throws Exception {
        return clienteComContas(true);
    }

    public ClienteDeTeste clienteComContas(boolean definirSenhaTransacao) throws Exception {
        String admin = loginAdmin(mvc).accessToken();
        String cpf = Cpfs.novo();
        String corpo = mvc.perform(comToken(post("/api/admin/clientes"), admin).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Cliente %s", "cpf": "%s", "senha": "%s", "segmento": "CORRENTISTA"}
                                """.formatted(cpf.substring(0, 4), cpf, SENHA_LOGIN)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long clienteId = ((Number) JsonPath.read(corpo, "$.id")).longValue();
        ContaDeTeste corrente = abrirConta(admin, clienteId, "CORRENTE");
        ContaDeTeste poupanca = abrirConta(admin, clienteId, "POUPANCA");

        String token = login(mvc, cpf, SENHA_LOGIN).accessToken();
        if (definirSenhaTransacao) {
            mvc.perform(comToken(put("/api/auth/senha-transacao"), token).contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"senhaLogin": "%s", "novaSenhaTransacao": "%s"}
                                    """.formatted(SENHA_LOGIN, SENHA_TRANSACAO)))
                    .andExpect(status().isNoContent());
        }
        return new ClienteDeTeste(clienteId, cpf, token, corrente, poupanca);
    }

    /** Cliente com saldo inicial na conta corrente, depositado pela API. */
    public ClienteDeTeste clienteComSaldo(String saldoCorrente) throws Exception {
        ClienteDeTeste cliente = clienteComContas();
        depositar(cliente.token(), cliente.corrente().id(), saldoCorrente).andExpect(status().isCreated());
        return cliente;
    }

    public ResultActions depositar(String token, long contaId, String valor) throws Exception {
        return mvc.perform(comToken(post("/api/contas/{id}/depositos", contaId), token).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"valor": %s, "forma": "DINHEIRO", "senhaTransacao": "%s"}
                        """.formatted(valor, SENHA_TRANSACAO)));
    }

    public ResultActions sacar(String token, long contaId, String valor, String senhaTransacao) throws Exception {
        return mvc.perform(comToken(post("/api/contas/{id}/saques", contaId), token).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"valor": %s, "senhaTransacao": "%s"}
                        """.formatted(valor, senhaTransacao)));
    }

    public ResultActions transferir(ClienteDeTeste origem, ContaDeTeste contaOrigem, ContaDeTeste destino, String cpfDestino,
            String valor, String chave) throws Exception {
        var requisicao = comToken(post("/api/transferencias"), origem.token()).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"contaOrigemId": %d, "agenciaDestino": "%s", "contaDestino": "%s", "cpfDestino": "%s",
                         "valor": %s, "senhaTransacao": "%s"}
                        """.formatted(contaOrigem.id(), destino.agencia(), destino.numero(), cpfDestino, valor, SENHA_TRANSACAO));
        if (chave != null) {
            requisicao.header("Idempotency-Key", chave);
        }
        return mvc.perform(requisicao);
    }

    public ResultActions transferir(ClienteDeTeste origem, ClienteDeTeste destino, String valor) throws Exception {
        return transferir(origem, origem.corrente(), destino.corrente(), destino.cpf(), valor, UUID.randomUUID().toString());
    }

    private ContaDeTeste abrirConta(String tokenAdmin, long clienteId, String tipo) throws Exception {
        String corpo = mvc.perform(comToken(post("/api/admin/clientes/{id}/contas", clienteId), tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"agenciaId": 1, "tipo": "%s"}
                                """.formatted(tipo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return new ContaDeTeste(
                ((Number) JsonPath.read(corpo, "$.id")).longValue(),
                JsonPath.read(corpo, "$.agencia"),
                JsonPath.read(corpo, "$.numero") + "-" + JsonPath.read(corpo, "$.digito"));
    }

    public record ClienteDeTeste(long id, String cpf, String token, ContaDeTeste corrente, ContaDeTeste poupanca) {
    }

    /** @param numero número com dígito, ex.: {@code 00012-3} */
    public record ContaDeTeste(long id, String agencia, String numero) {
    }

    public static BigDecimal valor(String valor) {
        return new BigDecimal(valor);
    }
}
