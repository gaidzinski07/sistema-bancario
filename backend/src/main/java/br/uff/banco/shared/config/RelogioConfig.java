package br.uff.banco.shared.config;

import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Relógio da aplicação. Regras que dependem de "agora" (expiração de tokens,
 * horário de operações) recebem o {@link Clock} injetado, para serem testáveis.
 * <p>
 * Precisão de microssegundos, igual à das colunas {@code TIMESTAMP(6)}: com
 * nanossegundos, o horário devolvido na resposta divergia do gravado (o MySQL
 * arredonda), e um comprovante relido do banco não batia com o original.
 */
@Configuration(proxyBeanMethods = false)
public class RelogioConfig {

    @Bean
    Clock relogio() {
        return Clock.tick(Clock.systemUTC(), Duration.of(1, ChronoUnit.MICROS));
    }
}
