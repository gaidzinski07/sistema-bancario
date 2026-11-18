package br.uff.banco.shared.config;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
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

    /**
     * Fuso do banco, usado para interpretar datas de calendário (ex.: o período do
     * extrato). Os instantes continuam gravados em UTC.
     */
    @Bean
    ZoneId fusoHorario(@Value("${banco.fuso-horario:America/Sao_Paulo}") String fuso) {
        return ZoneId.of(fuso);
    }
}
