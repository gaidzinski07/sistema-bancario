package br.uff.banco.investimentos;

import static br.uff.banco.suporte.ApiDeTeste.comToken;
import static br.uff.banco.suporte.ApiDeTeste.loginAdmin;
import static br.uff.banco.suporte.Cenarios.SENHA_TRANSACAO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.uff.banco.investimentos.application.InvestimentoService;
import br.uff.banco.shared.domain.Dinheiro;
import br.uff.banco.shared.domain.RegraNegocioException;
import br.uff.banco.suporte.Cenarios;
import br.uff.banco.suporte.Cenarios.ClienteDeTeste;
import br.uff.banco.suporte.TesteDeIntegracao;
import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Fundos, aplicação, carteira e resgate pela API.
 * <p>
 * Cada teste cria o próprio fundo (cota e mínimo controlados), para que as
 * atualizações de cota não interfiram entre si.
 */
@TesteDeIntegracao
class InvestimentosIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    InvestimentoService investimentos;

    Cenarios cenarios;
    String tokenAdmin;

    @BeforeEach
    void preparar() throws Exception {
        cenarios = new Cenarios(mvc);
        tokenAdmin = loginAdmin(mvc).accessToken();
    }

    @Nested
    class Aplicacao {

        @Test
        void debitaAContaCorrenteEAbreAPosicao() throws Exception {
            long fundo = criarFundo("10", "100");
            ClienteDeTeste cliente = cenarios.clienteComSaldo("1000.00");

            aplicar(cliente, cliente.corrente().id(), fundo, "400.00")
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.movimento.tipo").value("APLICACAO"))
                    .andExpect(jsonPath("$.movimento.quantidadeCotas").value(40))
                    .andExpect(jsonPath("$.saldoContaApos").value(600.00))
                    .andExpect(jsonPath("$.posicao.quantidadeCotas").value(40))
                    .andExpect(jsonPath("$.posicao.valorAtual").value(400.00))
                    .andExpect(jsonPath("$.posicao.rendimento").value(0));

            assertThat(saldo(cliente.corrente().id())).isEqualByComparingTo("600.00");
            assertThat(jdbc.queryForObject(
                    "SELECT contraparte FROM lancamento WHERE conta_id = ? AND tipo = 'APLICACAO'", String.class, cliente.corrente().id()))
                    .startsWith("Fundo IT");
            assertSaldoBateComLancamentos(cliente.corrente().id());
        }

        @Test
        void novaAplicacaoNoMesmoFundoSomaNaMesmaPosicao() throws Exception {
            long fundo = criarFundo("10", "100");
            ClienteDeTeste cliente = cenarios.clienteComSaldo("1000.00");

            long posicao = idDaPosicao(aplicar(cliente, cliente.corrente().id(), fundo, "200.00"));
            aplicar(cliente, cliente.corrente().id(), fundo, "300.00")
                    .andExpect(jsonPath("$.posicao.id").value(posicao))
                    .andExpect(jsonPath("$.posicao.quantidadeCotas").value(50));

            mvc.perform(comToken(get("/api/investimentos/posicoes/{id}", posicao), cliente.token()))
                    .andExpect(jsonPath("$.movimentos.length()").value(2))
                    .andExpect(jsonPath("$.posicao.valorAplicado").value(500.00));
        }

        @Test
        void regrasQueImpedemAAplicacao() throws Exception {
            long fundo = criarFundo("10", "500");
            ClienteDeTeste cliente = cenarios.clienteComSaldo("600.00");
            ClienteDeTeste outro = cenarios.clienteComSaldo("600.00");
            long conta = cliente.corrente().id();

            aplicar(cliente, conta, fundo, "499.99").andExpect(jsonPath("$.codigo").value("abaixo-da-aplicacao-minima"));
            aplicar(cliente, conta, fundo, "600.01").andExpect(jsonPath("$.codigo").value("saldo-insuficiente"));
            aplicar(cliente, cliente.poupanca().id(), fundo, "500.00").andExpect(jsonPath("$.codigo").value("conta-nao-corrente"));
            aplicar(cliente, conta, 999_999, "500.00").andExpect(status().isNotFound());
            aplicar(outro, conta, fundo, "500.00").andExpect(status().isNotFound());

            // nada foi gravado em nenhuma das tentativas
            assertThat(saldo(conta)).isEqualByComparingTo("600.00");
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM posicao WHERE conta_id = ?", Integer.class, conta)).isZero();
        }

        @Test
        void fundoDesativadoNaoAceitaAplicacao() throws Exception {
            long fundo = criarFundo("10", "100");
            ClienteDeTeste cliente = cenarios.clienteComSaldo("1000.00");
            mvc.perform(comToken(delete("/api/admin/fundos/{id}", fundo), tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ativo").value(false));

            aplicar(cliente, cliente.corrente().id(), fundo, "100.00")
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.codigo").value("fundo-inativo"));
            mvc.perform(comToken(get("/api/fundos"), cliente.token()))
                    .andExpect(jsonPath("$[?(@.id == " + fundo + ")]").isEmpty());
        }
    }

    @Nested
    class CarteiraEResgate {

        /** Critério de pronto do plano: rendimento bate com cotas × cota atual. */
        @Test
        void rendimentoAcompanhaACotaAtualizadaPeloAdmin() throws Exception {
            long fundo = criarFundo("10", "100");
            ClienteDeTeste cliente = cenarios.clienteComSaldo("1000.00");
            aplicar(cliente, cliente.corrente().id(), fundo, "1000.00");

            atualizarCota(fundo, "10.5").andExpect(status().isOk()).andExpect(jsonPath("$.cotaAtual").value(10.5));

            mvc.perform(comToken(get("/api/investimentos/posicoes"), cliente.token()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].quantidadeCotas").value(100))
                    .andExpect(jsonPath("$[0].cotaAtual").value(10.5))
                    .andExpect(jsonPath("$[0].valorAplicado").value(1000.00))
                    .andExpect(jsonPath("$[0].valorAtual").value(1050.00))
                    .andExpect(jsonPath("$[0].rendimento").value(50.00))
                    .andExpect(jsonPath("$[0].rentabilidadePercentual").value(5.00));
        }

        @Test
        void resgateParcialCreditaAContaEBaixaOCustoNaProporcao() throws Exception {
            long fundo = criarFundo("10", "100");
            ClienteDeTeste cliente = cenarios.clienteComSaldo("1000.00");
            long posicao = idDaPosicao(aplicar(cliente, cliente.corrente().id(), fundo, "1000.00"));
            atualizarCota(fundo, "12.5");                                                 // posição vale 1250

            resgatar(cliente, posicao, "{\"valor\": 500.00, \"senhaTransacao\": \"%s\"}")
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.movimento.tipo").value("RESGATE"))
                    .andExpect(jsonPath("$.movimento.quantidadeCotas").value(40))
                    .andExpect(jsonPath("$.saldoContaApos").value(500.00))
                    .andExpect(jsonPath("$.posicao.quantidadeCotas").value(60))
                    .andExpect(jsonPath("$.posicao.valorAplicado").value(600.00))
                    .andExpect(jsonPath("$.posicao.valorAtual").value(750.00))
                    .andExpect(jsonPath("$.posicao.rendimento").value(150.00));

            assertThat(saldo(cliente.corrente().id())).isEqualByComparingTo("500.00");
            assertSaldoBateComLancamentos(cliente.corrente().id());
        }

        @Test
        void resgateTotalPagaOValorDaCarteiraEAPosicaoSaiDaLista() throws Exception {
            long fundo = criarFundo("10", "100");
            ClienteDeTeste cliente = cenarios.clienteComSaldo("1000.00");
            long posicao = idDaPosicao(aplicar(cliente, cliente.corrente().id(), fundo, "1000.00"));
            atualizarCota(fundo, "10.33");

            String carteira = mvc.perform(comToken(get("/api/investimentos/posicoes"), cliente.token()))
                    .andReturn().getResponse().getContentAsString();
            Number valorNaCarteira = JsonPath.read(carteira, "$[0].valorAtual");

            resgatar(cliente, posicao, "{\"total\": true, \"senhaTransacao\": \"%s\"}")
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.movimento.valor").value(valorNaCarteira.doubleValue()))
                    .andExpect(jsonPath("$.posicao.quantidadeCotas").value(0));

            assertThat(saldo(cliente.corrente().id())).isEqualByComparingTo(BigDecimal.valueOf(valorNaCarteira.doubleValue()));
            mvc.perform(comToken(get("/api/investimentos/posicoes"), cliente.token())).andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        void naoResgataMaisDoQueAPosicaoVale() throws Exception {
            long fundo = criarFundo("10", "100");
            ClienteDeTeste cliente = cenarios.clienteComSaldo("1000.00");
            long posicao = idDaPosicao(aplicar(cliente, cliente.corrente().id(), fundo, "1000.00"));

            resgatar(cliente, posicao, "{\"valor\": 1000.01, \"senhaTransacao\": \"%s\"}")
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.codigo").value("saldo-investido-insuficiente"));
            assertThat(saldo(cliente.corrente().id())).isEqualByComparingTo("0.00");
        }

        /** Critério de pronto do plano. Legado: qualquer cliente resgatava o investimento de outro. */
        @Test
        void resgateDePosicaoDeOutroClienteDa404() throws Exception {
            long fundo = criarFundo("10", "100");
            ClienteDeTeste dono = cenarios.clienteComSaldo("1000.00");
            ClienteDeTeste outro = cenarios.clienteComContas();
            long posicao = idDaPosicao(aplicar(dono, dono.corrente().id(), fundo, "1000.00"));

            resgatar(outro, posicao, "{\"total\": true, \"senhaTransacao\": \"%s\"}").andExpect(status().isNotFound());
            mvc.perform(comToken(get("/api/investimentos/posicoes/{id}", posicao), outro.token())).andExpect(status().isNotFound());

            assertThat(jdbc.queryForObject("SELECT quantidade_cotas FROM posicao WHERE id = ?", BigDecimal.class, posicao))
                    .isEqualByComparingTo("100");
        }

        @Test
        void fundoDesativadoAindaPermiteResgatar() throws Exception {
            long fundo = criarFundo("10", "100");
            ClienteDeTeste cliente = cenarios.clienteComSaldo("1000.00");
            long posicao = idDaPosicao(aplicar(cliente, cliente.corrente().id(), fundo, "1000.00"));
            mvc.perform(comToken(delete("/api/admin/fundos/{id}", fundo), tokenAdmin));

            resgatar(cliente, posicao, "{\"total\": true, \"senhaTransacao\": \"%s\"}").andExpect(status().isCreated());
        }

        @Test
        void pedidoDeResgateTemQueSerValorOuTotal() throws Exception {
            long fundo = criarFundo("10", "100");
            ClienteDeTeste cliente = cenarios.clienteComSaldo("1000.00");
            long posicao = idDaPosicao(aplicar(cliente, cliente.corrente().id(), fundo, "1000.00"));

            resgatar(cliente, posicao, "{\"senhaTransacao\": \"%s\"}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erros[0].campo").value("valorOuTotal"));
            resgatar(cliente, posicao, "{\"valor\": 10, \"total\": true, \"senhaTransacao\": \"%s\"}")
                    .andExpect(status().isBadRequest());
        }

        @Test
        void aplicacaoEResgateAparecemNoExtratoDaConta() throws Exception {
            long fundo = criarFundo("10", "100");
            ClienteDeTeste cliente = cenarios.clienteComSaldo("1000.00");
            long posicao = idDaPosicao(aplicar(cliente, cliente.corrente().id(), fundo, "300.00"));
            resgatar(cliente, posicao, "{\"valor\": 100.00, \"senhaTransacao\": \"%s\"}");

            mvc.perform(comToken(get("/api/contas/{id}/extrato", cliente.corrente().id()), cliente.token()))
                    .andExpect(jsonPath("$.lancamentos.conteudo[1].tipo").value("APLICACAO"))
                    .andExpect(jsonPath("$.lancamentos.conteudo[1].natureza").value("DEBITO"))
                    .andExpect(jsonPath("$.lancamentos.conteudo[2].tipo").value("RESGATE"))
                    .andExpect(jsonPath("$.lancamentos.conteudo[2].natureza").value("CREDITO"))
                    .andExpect(jsonPath("$.saldoFinal").value(800.00));
        }
    }

    @Nested
    class AdministracaoDeFundos {

        @Test
        void cotaDoDiaVaiParaOHistoricoEUmaSegundaAtualizacaoCorrige() throws Exception {
            long fundo = criarFundo("10", "100");

            atualizarCota(fundo, "10.1");
            atualizarCota(fundo, "10.2");

            String hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString();
            mvc.perform(comToken(get("/api/fundos/{id}/cotacoes", fundo), tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].data").value(hoje))
                    .andExpect(jsonPath("$[0].valorCota").value(10.2));
        }

        @Test
        void validaCadastroECota() throws Exception {
            String nome = "Fundo IT " + UUID.randomUUID();
            criarFundo(nome, "10", "100").andExpect(status().isCreated());
            criarFundo(nome, "10", "100")
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.codigo").value("fundo-ja-existe"));
            criarFundo("Outro " + UUID.randomUUID(), "0", "100").andExpect(status().isBadRequest());

            long fundo = criarFundo("10", "100");
            atualizarCota(fundo, "-1").andExpect(status().isBadRequest());
            atualizarCota(fundo, "1.123456789").andExpect(status().isBadRequest());
        }

        @Test
        void clienteNaoAdministraFundos() throws Exception {
            ClienteDeTeste cliente = cenarios.clienteComContas();

            mvc.perform(comToken(get("/api/admin/fundos"), cliente.token())).andExpect(status().isForbidden());
            mvc.perform(comToken(get("/api/fundos"), cliente.token())).andExpect(status().isOk());
        }

        @Test
        void adminSemPapelDeClienteNaoInveste() throws Exception {
            mvc.perform(comToken(get("/api/investimentos/posicoes"), tokenAdmin)).andExpect(status().isForbidden());
        }
    }

    /** Aplicações e resgates simultâneos na mesma posição: cotas e saldo nunca divergem. */
    @Test
    void aplicacoesEResgatesSimultaneosMantemPosicaoEContaConsistentes() throws Exception {
        long fundo = criarFundo("10", "1");
        ClienteDeTeste cliente = cenarios.clienteComSaldo("1000.00");
        long conta = cliente.corrente().id();
        long posicao = idDaPosicao(aplicar(cliente, conta, fundo, "500.00"));

        int threads = 16;
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<?>> futuros = new ArrayList<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            for (int i = 0; i < threads; i++) {
                boolean aplica = i % 2 == 0;
                futuros.add(executor.submit(() -> {
                    largada.await();
                    return aplica
                            ? investimentos.aplicar(cliente.id(), conta, fundo, Dinheiro.of("40.00"), SENHA_TRANSACAO)
                            : investimentos.resgatar(cliente.id(), posicao, Dinheiro.of("30.00"), SENHA_TRANSACAO);
                }));
            }
            largada.countDown();
            for (Future<?> futuro : futuros) {
                try {
                    futuro.get(60, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    assertThat(e.getCause()).as("só regra de negócio pode recusar").isInstanceOf(RegraNegocioException.class);
                }
            }
        }

        BigDecimal cotas = jdbc.queryForObject("SELECT quantidade_cotas FROM posicao WHERE id = ?", BigDecimal.class, posicao);
        BigDecimal somaDosMovimentos = jdbc.queryForObject("""
                SELECT SUM(CASE tipo WHEN 'APLICACAO' THEN quantidade_cotas ELSE -quantidade_cotas END)
                  FROM movimento_investimento WHERE posicao_id = ?
                """, BigDecimal.class, posicao);
        assertThat(cotas).isEqualByComparingTo(somaDosMovimentos).isNotNegative();
        assertSaldoBateComLancamentos(conta);
        // dinheiro conservado: saldo + valor aplicado/resgatado pelos movimentos = depósito inicial
        BigDecimal liquidoInvestido = jdbc.queryForObject("""
                SELECT SUM(CASE tipo WHEN 'APLICACAO' THEN valor ELSE -valor END)
                  FROM movimento_investimento WHERE posicao_id = ?
                """, BigDecimal.class, posicao);
        assertThat(saldo(conta).add(liquidoInvestido)).isEqualByComparingTo("1000.00");
    }

    // ---------------------------------------------------------------- apoio

    private long criarFundo(String cota, String minimo) throws Exception {
        String corpo = criarFundo("Fundo IT " + UUID.randomUUID(), cota, minimo)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(corpo, "$.id")).longValue();
    }

    private ResultActions criarFundo(String nome, String cota, String minimo) throws Exception {
        return mvc.perform(comToken(post("/api/admin/fundos"), tokenAdmin).contentType(MediaType.APPLICATION_JSON).content("""
                {"nome": "%s", "cotaInicial": %s, "aplicacaoMinima": %s}
                """.formatted(nome, cota, minimo)));
    }

    private ResultActions atualizarCota(long fundo, String cota) throws Exception {
        return mvc.perform(comToken(put("/api/admin/fundos/{id}/cotacao", fundo), tokenAdmin).contentType(MediaType.APPLICATION_JSON)
                .content("{\"valorCota\": %s}".formatted(cota)));
    }

    private ResultActions aplicar(ClienteDeTeste cliente, long conta, long fundo, String valor) throws Exception {
        return mvc.perform(comToken(post("/api/investimentos/aplicacoes"), cliente.token()).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"contaId": %d, "fundoId": %d, "valor": %s, "senhaTransacao": "%s"}
                        """.formatted(conta, fundo, valor, SENHA_TRANSACAO)));
    }

    private ResultActions resgatar(ClienteDeTeste cliente, long posicao, String corpoComSenha) throws Exception {
        return mvc.perform(comToken(post("/api/investimentos/posicoes/{id}/resgates", posicao), cliente.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoComSenha.formatted(SENHA_TRANSACAO)));
    }

    private static long idDaPosicao(ResultActions aplicacao) throws Exception {
        String corpo = aplicacao.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(corpo, "$.posicao.id")).longValue();
    }

    private BigDecimal saldo(long conta) {
        return jdbc.queryForObject("SELECT saldo FROM conta WHERE id = ?", BigDecimal.class, conta);
    }

    private void assertSaldoBateComLancamentos(long conta) {
        BigDecimal soma = jdbc.queryForObject("""
                SELECT COALESCE(SUM(CASE natureza WHEN 'CREDITO' THEN valor ELSE -valor END), 0)
                  FROM lancamento WHERE conta_id = ?
                """, BigDecimal.class, conta);
        assertThat(saldo(conta)).isEqualByComparingTo(soma);
    }
}
