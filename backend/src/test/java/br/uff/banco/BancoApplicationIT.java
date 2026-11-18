package br.uff.banco;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.uff.banco.suporte.TesteDeIntegracao;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

/** Sobe a aplicação inteira contra um MySQL real e confere migrations, seed e invariantes do schema. */
@TesteDeIntegracao
class BancoApplicationIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void healthCheckEstaUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void endpointsNaoPublicosExigemAutenticacao() throws Exception {
        mvc.perform(get("/api/admin/clientes")).andExpect(status().isUnauthorized());
    }

    @Test
    void migrationsESeedForamAplicados() {
        List<String> versoes = jdbc.queryForList(
                "SELECT version FROM flyway_schema_history WHERE success = 1 ORDER BY installed_rank", String.class);
        assertThat(versoes).containsExactly("1", "1.1", "2", "3");

        // O banco é compartilhado entre as classes de teste: confere os registros do seed, não contagens.
        assertThat(jdbc.queryForList("SELECT id FROM usuario WHERE id <= 4", Long.class)).containsExactly(1L, 2L, 3L, 4L);
        assertThat(jdbc.queryForList("SELECT id FROM conta WHERE id <= 5", Long.class)).hasSize(5);
    }

    @Test
    void v2ContinuaANumeracaoDasContasDoSeedPorAgencia() {
        // Agência 1 tem as contas 00001 e 00002 no seed; a próxima é a 3 (ou mais, se outro teste já abriu contas).
        Long proximo = jdbc.queryForObject("SELECT proximo_numero_conta FROM agencia WHERE id = 1", Long.class);
        assertThat(proximo).isGreaterThanOrEqualTo(3L);
    }

    @Test
    void saldoDeCadaContaBateComOsLancamentos() {
        List<String> divergentes = jdbc.queryForList("""
                SELECT c.id
                  FROM conta c
                  LEFT JOIN lancamento l ON l.conta_id = c.id
                 GROUP BY c.id, c.saldo
                HAVING c.saldo <> COALESCE(SUM(CASE l.natureza WHEN 'CREDITO' THEN l.valor ELSE -l.valor END), 0)
                """, String.class);
        assertThat(divergentes).isEmpty();
    }

    @Test
    void bancoRecusaSaldoNegativo() {
        assertThatThrownBy(() -> jdbc.update("UPDATE conta SET saldo = ? WHERE id = 1", new BigDecimal("-0.01")))
                .hasMessageContaining("ck_conta_saldo_nao_negativo");
    }
}
