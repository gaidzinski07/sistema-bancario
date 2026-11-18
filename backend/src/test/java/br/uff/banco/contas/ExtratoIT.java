package br.uff.banco.contas;

import static br.uff.banco.suporte.ApiDeTeste.comToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.uff.banco.suporte.Cenarios;
import br.uff.banco.suporte.Cenarios.ClienteDeTeste;
import br.uff.banco.suporte.TesteDeIntegracao;
import com.jayway.jsonpath.JsonPath;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Extrato e detalhe de conta.
 * <p>
 * Legado: a tela de saldo tinha valores fixos (agência 0123, R$ 85,00) e o extrato
 * mostrava o mesmo valor nas colunas "Entrada" e "Saída".
 */
@TesteDeIntegracao
class ExtratoIT {

    private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    Cenarios cenarios;

    @BeforeEach
    void preparar() {
        cenarios = new Cenarios(mvc);
    }

    @Test
    void detalheMostraOSaldoReal() throws Exception {
        ClienteDeTeste cliente = cenarios.clienteComSaldo("123.45");

        mvc.perform(comToken(get("/api/contas/{id}", cliente.corrente().id()), cliente.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(123.45))
                .andExpect(jsonPath("$.agencia").value("0001"))
                .andExpect(jsonPath("$.tipo").value("CORRENTE"));
    }

    @Test
    void extratoDeHojeTrazOsLancamentosEmOrdemComSaldoAposCadaUm() throws Exception {
        ClienteDeTeste cliente = cenarios.clienteComContas();
        long conta = cliente.corrente().id();
        cenarios.depositar(cliente.token(), conta, "100.00");
        cenarios.sacar(cliente.token(), conta, "30.00", Cenarios.SENHA_TRANSACAO);
        cenarios.depositar(cliente.token(), conta, "50.00");

        extrato(cliente, conta, hoje(), hoje())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoInicial").value(0))
                .andExpect(jsonPath("$.totalCreditos").value(150.00))
                .andExpect(jsonPath("$.totalDebitos").value(30.00))
                .andExpect(jsonPath("$.saldoFinal").value(120.00))
                .andExpect(jsonPath("$.fusoHorario").value("America/Sao_Paulo"))
                .andExpect(jsonPath("$.lancamentos.conteudo.length()").value(3))
                .andExpect(jsonPath("$.lancamentos.conteudo[0].natureza").value("CREDITO"))
                .andExpect(jsonPath("$.lancamentos.conteudo[0].saldoApos").value(100.00))
                .andExpect(jsonPath("$.lancamentos.conteudo[1].tipo").value("SAQUE"))
                .andExpect(jsonPath("$.lancamentos.conteudo[1].natureza").value("DEBITO"))
                .andExpect(jsonPath("$.lancamentos.conteudo[1].valor").value(30.00))
                .andExpect(jsonPath("$.lancamentos.conteudo[1].saldoApos").value(70.00))
                .andExpect(jsonPath("$.lancamentos.conteudo[2].saldoApos").value(120.00));
    }

    @Test
    void transferenciaApareceComAContraparteNosDoisExtratos() throws Exception {
        ClienteDeTeste origem = cenarios.clienteComSaldo("100.00");
        ClienteDeTeste destino = cenarios.clienteComContas();
        cenarios.transferir(origem, destino, "40.00").andExpect(status().isCreated());

        extrato(origem, origem.corrente().id(), hoje(), hoje())
                .andExpect(jsonPath("$.lancamentos.conteudo[1].tipo").value("TRANSFERENCIA_ENVIADA"))
                .andExpect(jsonPath("$.lancamentos.conteudo[1].contraparte")
                        .value(org.hamcrest.Matchers.containsString(destino.corrente().numero())));
        extrato(destino, destino.corrente().id(), hoje(), hoje())
                .andExpect(jsonPath("$.lancamentos.conteudo[0].tipo").value("TRANSFERENCIA_RECEBIDA"))
                .andExpect(jsonPath("$.lancamentos.conteudo[0].contraparte")
                        .value(org.hamcrest.Matchers.containsString(origem.corrente().numero())));
    }

    @Test
    void periodoFiltraOsLancamentosESaldoInicialVemDoAnterior() throws Exception {
        ClienteDeTeste cliente = cenarios.clienteComContas();
        long conta = cliente.corrente().id();
        cenarios.depositar(cliente.token(), conta, "100.00");                       // saldo 100
        cenarios.sacar(cliente.token(), conta, "25.00", Cenarios.SENHA_TRANSACAO); // saldo 75
        cenarios.depositar(cliente.token(), conta, "10.00");                        // saldo 85
        moverLancamentos(conta, "2026-01-05T15:00:00Z", "2026-02-10T15:00:00Z", "2026-03-15T15:00:00Z");

        extrato(cliente, conta, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28))
                .andExpect(jsonPath("$.saldoInicial").value(100.00))
                .andExpect(jsonPath("$.totalCreditos").value(0))
                .andExpect(jsonPath("$.totalDebitos").value(25.00))
                .andExpect(jsonPath("$.saldoFinal").value(75.00))
                .andExpect(jsonPath("$.lancamentos.conteudo.length()").value(1))
                .andExpect(jsonPath("$.lancamentos.conteudo[0].tipo").value("SAQUE"));

        // período sem nenhum lançamento: saldo inicial = final
        extrato(cliente, conta, LocalDate.of(2026, 2, 11), LocalDate.of(2026, 3, 14))
                .andExpect(jsonPath("$.saldoInicial").value(75.00))
                .andExpect(jsonPath("$.saldoFinal").value(75.00))
                .andExpect(jsonPath("$.lancamentos.totalElementos").value(0));

        // período que cobre tudo
        extrato(cliente, conta, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31))
                .andExpect(jsonPath("$.saldoInicial").value(0))
                .andExpect(jsonPath("$.saldoFinal").value(85.00))
                .andExpect(jsonPath("$.lancamentos.totalElementos").value(3));
    }

    @Test
    void diaDoExtratoSegueOFusoDoBancoENaoOUtc() throws Exception {
        ClienteDeTeste cliente = cenarios.clienteComContas();
        long conta = cliente.corrente().id();
        cenarios.depositar(cliente.token(), conta, "10.00");
        // 02:30 UTC do dia 10 = 23:30 do dia 9 em Brasília
        moverLancamentos(conta, "2026-04-10T02:30:00Z");

        extrato(cliente, conta, LocalDate.of(2026, 4, 9), LocalDate.of(2026, 4, 9))
                .andExpect(jsonPath("$.lancamentos.totalElementos").value(1));
        extrato(cliente, conta, LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 10))
                .andExpect(jsonPath("$.lancamentos.totalElementos").value(0))
                .andExpect(jsonPath("$.saldoInicial").value(10.00));
    }

    @Test
    void paginaOsLancamentosMasOsTotaisSaoDoPeriodoInteiro() throws Exception {
        ClienteDeTeste cliente = cenarios.clienteComContas();
        long conta = cliente.corrente().id();
        for (int i = 1; i <= 5; i++) {
            cenarios.depositar(cliente.token(), conta, i + "0.00");                // 10, 20, 30, 40, 50
        }

        String paginaZero = extrato(cliente, conta, hoje(), hoje(), 0, 2)
                .andExpect(jsonPath("$.lancamentos.conteudo.length()").value(2))
                .andExpect(jsonPath("$.lancamentos.totalElementos").value(5))
                .andExpect(jsonPath("$.lancamentos.totalPaginas").value(3))
                .andExpect(jsonPath("$.totalCreditos").value(150.00))
                .andExpect(jsonPath("$.saldoFinal").value(150.00))
                .andReturn().getResponse().getContentAsString();
        extrato(cliente, conta, hoje(), hoje(), 2, 2)
                .andExpect(jsonPath("$.lancamentos.conteudo.length()").value(1))
                .andExpect(jsonPath("$.lancamentos.conteudo[0].saldoApos").value(150.00));

        List<Number> saldos = JsonPath.read(paginaZero, "$.lancamentos.conteudo[*].saldoApos");
        assertThat(saldos).extracting(Number::doubleValue).containsExactly(10.0, 30.0);
    }

    @Test
    void semDatasUsaOsUltimos30Dias() throws Exception {
        ClienteDeTeste cliente = cenarios.clienteComSaldo("10.00");

        mvc.perform(comToken(get("/api/contas/{id}/extrato", cliente.corrente().id()), cliente.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ate").value(hoje().toString()))
                .andExpect(jsonPath("$.de").value(hoje().minusDays(29).toString()))
                .andExpect(jsonPath("$.lancamentos.totalElementos").value(1));
    }

    @Test
    void contaDeOutroClienteDa404() throws Exception {
        ClienteDeTeste dono = cenarios.clienteComSaldo("10.00");
        ClienteDeTeste outro = cenarios.clienteComContas();

        extrato(outro, dono.corrente().id(), hoje(), hoje()).andExpect(status().isNotFound());
        mvc.perform(comToken(get("/api/contas/{id}", dono.corrente().id()), outro.token())).andExpect(status().isNotFound());
    }

    @Test
    void validaOPeriodoEAPaginacao() throws Exception {
        ClienteDeTeste cliente = cenarios.clienteComContas();
        long conta = cliente.corrente().id();

        extrato(cliente, conta, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 2, 1))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("periodo-invalido"));
        extrato(cliente, conta, LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 2))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("periodo-invalido"));
        extrato(cliente, conta, hoje(), hoje(), 0, 101)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("dados-invalidos"));
        mvc.perform(comToken(get("/api/contas/{id}/extrato", conta).param("de", "2026-13-45"), cliente.token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("dados-invalidos"))
                .andExpect(jsonPath("$.erros[0].campo").value("de"));
    }

    private ResultActions extrato(ClienteDeTeste cliente, long conta, LocalDate de, LocalDate ate) throws Exception {
        return extrato(cliente, conta, de, ate, 0, 50);
    }

    private ResultActions extrato(ClienteDeTeste cliente, long conta, LocalDate de, LocalDate ate, int pagina, int tamanho)
            throws Exception {
        return mvc.perform(comToken(get("/api/contas/{id}/extrato", conta), cliente.token())
                .param("de", de.toString())
                .param("ate", ate.toString())
                .param("pagina", String.valueOf(pagina))
                .param("tamanho", String.valueOf(tamanho)));
    }

    /**
     * Reposiciona no tempo os lançamentos da conta (em ordem de ID), para simular
     * operações em datas passadas. A ordem cronológica é mantida, então os
     * {@code saldo_apos} continuam coerentes.
     */
    private void moverLancamentos(long conta, String... instantes) {
        List<Long> ids = jdbc.queryForList("SELECT id FROM lancamento WHERE conta_id = ? ORDER BY id", Long.class, conta);
        assertThat(ids).hasSize(instantes.length);
        for (int i = 0; i < ids.size(); i++) {
            jdbc.update("UPDATE lancamento SET ocorrido_em = ? WHERE id = ?", Timestamp.from(Instant.parse(instantes[i])), ids.get(i));
        }
    }

    private static LocalDate hoje() {
        return LocalDate.now(BRASILIA);
    }
}
