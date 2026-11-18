package br.uff.banco;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

/**
 * Sobe a aplicação inteira contra um MySQL real (Testcontainers), aplicando as
 * migrations e o seed de desenvolvimento.
 * <p>
 * Sem Docker disponível o teste é pulado (localmente); no CI ele sempre roda.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Testcontainers(disabledWithoutDocker = true)
class BancoApplicationIT {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.4");

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
        mvc.perform(get("/api/contas")).andExpect(status().isUnauthorized());
    }

    @Test
    void migrationsESeedForamAplicados() {
        List<String> versoes = jdbc.queryForList(
                "SELECT version FROM flyway_schema_history WHERE success = 1 ORDER BY installed_rank", String.class);
        assertThat(versoes).containsExactly("1", "1.1");

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM usuario", Integer.class)).isEqualTo(4);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM conta", Integer.class)).isEqualTo(5);
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
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                jdbc.update("UPDATE conta SET saldo = ? WHERE id = 1", new BigDecimal("-0.01")))
                .hasMessageContaining("ck_conta_saldo_nao_negativo");
    }
}
