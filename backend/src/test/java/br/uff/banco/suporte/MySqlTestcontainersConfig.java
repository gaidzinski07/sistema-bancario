package br.uff.banco.suporte;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;

/**
 * MySQL real (mesma imagem do docker-compose) como bean: o Spring gerencia o ciclo
 * de vida e o cache de contexto reaproveita o mesmo container entre as classes de
 * teste com a mesma configuração.
 */
@TestConfiguration(proxyBeanMethods = false)
public class MySqlTestcontainersConfig {

    @Bean
    @ServiceConnection
    MySQLContainer mysql() {
        return new MySQLContainer("mysql:8.4");
    }
}
