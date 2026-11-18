package br.uff.banco.contas;

import static br.uff.banco.suporte.ApiDeTeste.comToken;
import static br.uff.banco.suporte.ApiDeTeste.loginAdmin;
import static br.uff.banco.suporte.Cenarios.SENHA_TRANSACAO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.uff.banco.suporte.Cenarios;
import br.uff.banco.suporte.Cenarios.ClienteDeTeste;
import br.uff.banco.suporte.TesteDeIntegracao;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Depósito, saque e transferência pela API.
 * <p>
 * Cada caso marcado "Legado" reproduz um defeito do sistema JSP (docs/plano-migracao.md,
 * seções 2.1 a 2.3) e prova que ele não existe mais.
 */
@TesteDeIntegracao
class OperacoesIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    Cenarios cenarios;

    @BeforeEach
    void preparar() {
        cenarios = new Cenarios(mvc);
    }

    @Nested
    class Deposito {

        @Test
        void creditaNaContaEscolhidaComComprovante() throws Exception {
            ClienteDeTeste cliente = cenarios.clienteComContas();

            // Legado: o depósito ia sempre para a conta corrente, qualquer que fosse a escolha.
            mvc.perform(comToken(post("/api/contas/{id}/depositos", cliente.poupanca().id()), cliente.token())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"valor": 250.50, "forma": "CHEQUE", "senhaTransacao": "%s"}
                                    """.formatted(SENHA_TRANSACAO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.tipo").value("DEPOSITO"))
                    .andExpect(jsonPath("$.descricao").value("Depósito em cheque"))
                    .andExpect(jsonPath("$.valor").value(250.50))
                    .andExpect(jsonPath("$.saldoApos").value(250.50))
                    .andExpect(jsonPath("$.conta.tipo").value("POUPANCA"));

            assertThat(saldo(cliente.poupanca().id())).isEqualByComparingTo("250.50");
            assertThat(saldo(cliente.corrente().id())).isEqualByComparingTo("0");
            assertThat(jdbc.queryForObject("SELECT forma FROM deposito WHERE conta_id = ?", String.class, cliente.poupanca().id()))
                    .isEqualTo("CHEQUE");
            assertSaldoBateComLancamentos(cliente.poupanca().id());
        }

        @ParameterizedTest(name = "valor={0}")
        @ValueSource(strings = {"0", "-10", "10.005"})
        void recusaValorInvalidoSemArredondar(String valor) throws Exception {
            ClienteDeTeste cliente = cenarios.clienteComContas();

            cenarios.depositar(cliente.token(), cliente.corrente().id(), valor)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erros.length()").value(1))
                    .andExpect(jsonPath("$.erros[0].campo").value("valor"))
                    .andExpect(jsonPath("$.erros[0].mensagem").value("deve ser um valor positivo com até 2 casas decimais"));
            assertThat(saldo(cliente.corrente().id())).isEqualByComparingTo("0");
        }
    }

    @Nested
    class Saque {

        @Test
        void debitaEGeraLancamento() throws Exception {
            ClienteDeTeste cliente = cenarios.clienteComSaldo("100.00");

            cenarios.sacar(cliente.token(), cliente.corrente().id(), "30.00", SENHA_TRANSACAO)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.tipo").value("SAQUE"))
                    .andExpect(jsonPath("$.saldoApos").value(70.00));

            assertThat(saldo(cliente.corrente().id())).isEqualByComparingTo("70.00");
            assertSaldoBateComLancamentos(cliente.corrente().id());
        }

        /** Legado: Realizarsaque não conferia o saldo; a conta ficava negativa. */
        @Test
        void semSaldoSuficienteEhRecusadoENadaEhGravado() throws Exception {
            ClienteDeTeste cliente = cenarios.clienteComSaldo("50.00");

            cenarios.sacar(cliente.token(), cliente.corrente().id(), "50.01", SENHA_TRANSACAO)
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.codigo").value("saldo-insuficiente"));

            assertThat(saldo(cliente.corrente().id())).isEqualByComparingTo("50.00");
            assertThat(contar("SELECT COUNT(*) FROM saque WHERE conta_id = ?", cliente.corrente().id())).isZero();
            assertSaldoBateComLancamentos(cliente.corrente().id());
        }
    }

    @Nested
    class Titularidade {

        /** Legado: depósito, saque e transferência usavam id_logado = 1 para qualquer usuário. */
        @Test
        void clienteNaoOperaContaDeOutroCliente() throws Exception {
            ClienteDeTeste vitima = cenarios.clienteComSaldo("500.00");
            ClienteDeTeste atacante = cenarios.clienteComContas();

            cenarios.sacar(atacante.token(), vitima.corrente().id(), "10.00", SENHA_TRANSACAO)
                    .andExpect(status().isNotFound());
            cenarios.depositar(atacante.token(), vitima.corrente().id(), "10.00")
                    .andExpect(status().isNotFound());
            cenarios.transferir(atacante, vitima.corrente(), atacante.corrente(), atacante.cpf(), "10.00", UUID.randomUUID().toString())
                    .andExpect(status().isNotFound());

            assertThat(saldo(vitima.corrente().id())).isEqualByComparingTo("500.00");
        }

        @Test
        void listaSoAsContasDoProprioCliente() throws Exception {
            ClienteDeTeste cliente = cenarios.clienteComContas();

            mvc.perform(comToken(get("/api/contas"), cliente.token()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(cliente.corrente().id()))
                    .andExpect(jsonPath("$[1].id").value(cliente.poupanca().id()));
        }

        @Test
        void administradorSemPapelDeClienteNaoAcessaContas() throws Exception {
            mvc.perform(comToken(get("/api/contas"), loginAdmin(mvc).accessToken()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class Transferencia {

        @Test
        void moveDinheiroEntreClientesComLancamentosNasDuasContas() throws Exception {
            ClienteDeTeste origem = cenarios.clienteComSaldo("1000.00");
            ClienteDeTeste destino = cenarios.clienteComContas();

            cenarios.transferir(origem, destino, "250.00")
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.valor").value(250.00))
                    .andExpect(jsonPath("$.saldoOrigemApos").value(750.00))
                    .andExpect(jsonPath("$.destino.conta").value(destino.corrente().numero()))
                    .andExpect(jsonPath("$.favorecido").value(org.hamcrest.Matchers.startsWith("Cliente ")));

            assertThat(saldo(origem.corrente().id())).isEqualByComparingTo("750.00");
            assertThat(saldo(destino.corrente().id())).isEqualByComparingTo("250.00");
            assertThat(jdbc.queryForObject("SELECT tipo FROM lancamento WHERE conta_id = ?", String.class, destino.corrente().id()))
                    .isEqualTo("TRANSFERENCIA_RECEBIDA");
            assertSaldoBateComLancamentos(origem.corrente().id());
            assertSaldoBateComLancamentos(destino.corrente().id());
        }

        /** Legado: transferências da poupança eram gravadas como se fossem da corrente. */
        @Test
        void entreCorrenteEPoupancaDoMesmoClienteFicaRegistradaEmCadaConta() throws Exception {
            ClienteDeTeste cliente = cenarios.clienteComSaldo("100.00");

            cenarios.transferir(cliente, cliente.corrente(), cliente.poupanca(), cliente.cpf(), "40.00", UUID.randomUUID().toString())
                    .andExpect(status().isCreated());

            assertThat(saldo(cliente.corrente().id())).isEqualByComparingTo("60.00");
            assertThat(saldo(cliente.poupanca().id())).isEqualByComparingTo("40.00");
            assertThat(jdbc.queryForObject(
                    "SELECT COUNT(*) FROM transferencia WHERE conta_origem_id = ? AND conta_destino_id = ?",
                    Integer.class, cliente.corrente().id(), cliente.poupanca().id())).isEqualTo(1);
        }

        /** Legado: sem conferência de saldo. */
        @Test
        void semSaldoEhRecusada() throws Exception {
            ClienteDeTeste origem = cenarios.clienteComSaldo("10.00");
            ClienteDeTeste destino = cenarios.clienteComContas();

            cenarios.transferir(origem, destino, "10.01")
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.codigo").value("saldo-insuficiente"));

            assertThat(saldo(origem.corrente().id())).isEqualByComparingTo("10.00");
            assertThat(saldo(destino.corrente().id())).isEqualByComparingTo("0");
            assertThat(contar("SELECT COUNT(*) FROM transferencia WHERE conta_origem_id = ?", origem.corrente().id())).isZero();
        }

        /** Legado: valor negativo "puxava" dinheiro do destinatário. */
        @ParameterizedTest(name = "valor={0}")
        @ValueSource(strings = {"-100.00", "0"})
        void valorNaoPositivoEhRecusado(String valor) throws Exception {
            ClienteDeTeste origem = cenarios.clienteComSaldo("10.00");
            ClienteDeTeste destino = cenarios.clienteComSaldo("500.00");

            cenarios.transferir(origem, destino, valor).andExpect(status().isBadRequest());

            assertThat(saldo(destino.corrente().id())).isEqualByComparingTo("500.00");
        }

        /** Legado: agência e CPF do destinatário eram lidos e ignorados. */
        @Test
        void dadosDoDestinoTemQueBater() throws Exception {
            ClienteDeTeste origem = cenarios.clienteComSaldo("100.00");
            ClienteDeTeste destino = cenarios.clienteComContas();
            var conta = destino.corrente();
            String digitoErrado = conta.numero().substring(0, conta.numero().length() - 1)
                    + ((Character.getNumericValue(conta.numero().charAt(conta.numero().length() - 1)) + 1) % 10);

            // CPF de outra pessoa
            cenarios.transferir(origem, origem.corrente(), conta, origem.cpf(), "1.00", UUID.randomUUID().toString())
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.codigo").value("destino-nao-encontrado"));
            // dígito errado
            cenarios.transferir(origem, origem.corrente(), new Cenarios.ContaDeTeste(conta.id(), conta.agencia(), digitoErrado),
                            destino.cpf(), "1.00", UUID.randomUUID().toString())
                    .andExpect(jsonPath("$.codigo").value("destino-nao-encontrado"));
            // agência errada
            cenarios.transferir(origem, origem.corrente(), new Cenarios.ContaDeTeste(conta.id(), "0002", conta.numero()),
                            destino.cpf(), "1.00", UUID.randomUUID().toString())
                    .andExpect(jsonPath("$.codigo").value("destino-nao-encontrado"));

            assertThat(saldo(origem.corrente().id())).isEqualByComparingTo("100.00");
        }

        @Test
        void paraAMesmaContaEhRecusada() throws Exception {
            ClienteDeTeste cliente = cenarios.clienteComSaldo("100.00");

            cenarios.transferir(cliente, cliente.corrente(), cliente.corrente(), cliente.cpf(), "1.00", UUID.randomUUID().toString())
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.codigo").value("mesma-conta"));
        }

        /** Legado: débito, crédito e registro em commits separados. Aqui, falha no crédito desfaz o débito. */
        @Test
        void falhaNaContaDeDestinoDesfazOOperacaoInteira() throws Exception {
            ClienteDeTeste origem = cenarios.clienteComSaldo("100.00");
            ClienteDeTeste destino = cenarios.clienteComContas();
            jdbc.update("UPDATE conta SET status = 'BLOQUEADA' WHERE id = ?", destino.corrente().id());

            cenarios.transferir(origem, destino, "60.00")
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.codigo").value("conta-nao-ativa"));

            assertThat(saldo(origem.corrente().id())).isEqualByComparingTo("100.00");
            assertThat(contar("SELECT COUNT(*) FROM transferencia WHERE conta_origem_id = ?", origem.corrente().id())).isZero();
            assertSaldoBateComLancamentos(origem.corrente().id());
        }
    }

    @Nested
    class Idempotencia {

        @Test
        void reenvioComAMesmaChaveDevolveOComprovanteOriginalSemTransferirDeNovo() throws Exception {
            ClienteDeTeste origem = cenarios.clienteComSaldo("100.00");
            ClienteDeTeste destino = cenarios.clienteComContas();
            String chave = UUID.randomUUID().toString();

            String primeiro = cenarios.transferir(origem, origem.corrente(), destino.corrente(), destino.cpf(), "30.00", chave)
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            String segundo = cenarios.transferir(origem, origem.corrente(), destino.corrente(), destino.cpf(), "30.00", chave)
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            assertThat(segundo).isEqualTo(primeiro);
            assertThat(saldo(origem.corrente().id())).isEqualByComparingTo("70.00");
            assertThat(saldo(destino.corrente().id())).isEqualByComparingTo("30.00");
        }

        @Test
        void mesmaChaveComOutroValorEhRecusada() throws Exception {
            ClienteDeTeste origem = cenarios.clienteComSaldo("100.00");
            ClienteDeTeste destino = cenarios.clienteComContas();
            String chave = UUID.randomUUID().toString();
            cenarios.transferir(origem, origem.corrente(), destino.corrente(), destino.cpf(), "30.00", chave)
                    .andExpect(status().isCreated());

            cenarios.transferir(origem, origem.corrente(), destino.corrente(), destino.cpf(), "31.00", chave)
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.codigo").value("idempotencia-conflitante"));
            assertThat(saldo(origem.corrente().id())).isEqualByComparingTo("70.00");
        }

        @Test
        void chaveDeOutroClienteNaoVazaOComprovante() throws Exception {
            ClienteDeTeste origem = cenarios.clienteComSaldo("100.00");
            ClienteDeTeste destino = cenarios.clienteComContas();
            String chave = UUID.randomUUID().toString();
            cenarios.transferir(origem, origem.corrente(), destino.corrente(), destino.cpf(), "30.00", chave)
                    .andExpect(status().isCreated());

            ClienteDeTeste outro = cenarios.clienteComSaldo("100.00");
            cenarios.transferir(outro, origem.corrente(), destino.corrente(), destino.cpf(), "30.00", chave)
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.codigo").value("idempotencia-conflitante"));
        }

        @Test
        void semHeaderIdempotencyKeyEhRecusada() throws Exception {
            ClienteDeTeste origem = cenarios.clienteComSaldo("100.00");
            ClienteDeTeste destino = cenarios.clienteComContas();

            cenarios.transferir(origem, origem.corrente(), destino.corrente(), destino.cpf(), "1.00", null)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("requisicao-invalida"))
                    .andExpect(jsonPath("$.detail").value("O header Idempotency-Key é obrigatório."));
        }
    }

    @Nested
    class SenhaDeTransacao {

        @Test
        void semSenhaDefinidaNaoOpera() throws Exception {
            ClienteDeTeste cliente = cenarios.clienteComContas(false);

            cenarios.depositar(cliente.token(), cliente.corrente().id(), "10.00")
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.codigo").value("senha-transacao-nao-definida"));
        }

        @Test
        void senhaErradaNaoOperaEContaTentativas() throws Exception {
            ClienteDeTeste cliente = cenarios.clienteComSaldo("100.00");

            cenarios.sacar(cliente.token(), cliente.corrente().id(), "10.00", "0000")
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.codigo").value("senha-transacao-invalida"))
                    .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("2 tentativas restantes")));
            assertThat(saldo(cliente.corrente().id())).isEqualByComparingTo("100.00");
        }

        /** Com 4 dígitos, sem limite de tentativas a senha cairia por força bruta. */
        @Test
        void tresErrosBloqueiamInclusiveASenhaCerta() throws Exception {
            ClienteDeTeste cliente = cenarios.clienteComSaldo("100.00");
            for (int i = 0; i < 3; i++) {
                cenarios.sacar(cliente.token(), cliente.corrente().id(), "10.00", "0000");
            }

            cenarios.sacar(cliente.token(), cliente.corrente().id(), "10.00", SENHA_TRANSACAO)
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.codigo").value("senha-transacao-bloqueada"));
            assertThat(saldo(cliente.corrente().id())).isEqualByComparingTo("100.00");
        }

        @Test
        void definirExigeASenhaDeLoginEFormatoNumerico() throws Exception {
            ClienteDeTeste cliente = cenarios.clienteComContas(false);

            mvc.perform(comToken(put("/api/auth/senha-transacao"), cliente.token()).contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"senhaLogin": "senha-errada", "novaSenhaTransacao": "1234"}
                                    """))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.codigo").value("senha-login-incorreta"));
            mvc.perform(comToken(put("/api/auth/senha-transacao"), cliente.token()).contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"senhaLogin": "%s", "novaSenhaTransacao": "12ab"}
                                    """.formatted(Cenarios.SENHA_LOGIN)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void bancoGuardaSoOHash() throws Exception {
            ClienteDeTeste cliente = cenarios.clienteComContas();

            String guardado = jdbc.queryForObject("SELECT senha_transacao_hash FROM usuario WHERE id = ?", String.class, cliente.id());
            assertThat(guardado).startsWith("{bcrypt}").doesNotContain(SENHA_TRANSACAO);
        }
    }

    private BigDecimal saldo(long contaId) {
        return jdbc.queryForObject("SELECT saldo FROM conta WHERE id = ?", BigDecimal.class, contaId);
    }

    private int contar(String sql, Object... parametros) {
        return jdbc.queryForObject(sql, Integer.class, parametros);
    }

    private void assertSaldoBateComLancamentos(long contaId) {
        BigDecimal somaLancamentos = jdbc.queryForObject("""
                SELECT COALESCE(SUM(CASE natureza WHEN 'CREDITO' THEN valor ELSE -valor END), 0)
                  FROM lancamento WHERE conta_id = ?
                """, BigDecimal.class, contaId);
        assertThat(saldo(contaId)).as("saldo da conta %d = soma dos lançamentos", contaId).isEqualByComparingTo(somaLancamentos);
    }
}
