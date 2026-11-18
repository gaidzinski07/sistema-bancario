package br.uff.banco.shared.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Relógio da aplicação. Regras que dependem de "agora" (expiração de tokens,
 * horário de operações) recebem o {@link Clock} injetado, para serem testáveis.
 */
@Configuration(proxyBeanMethods = false)
public class RelogioConfig {

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }
}
