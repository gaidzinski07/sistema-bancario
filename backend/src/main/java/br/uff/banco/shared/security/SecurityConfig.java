package br.uff.banco.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Configuração base de segurança.
 * <p>
 * Fase 1: tudo fechado, exceto health check e documentação da API. O login por
 * CPF/senha, os papéis ADMIN/CLIENTE e o logout entram na Fase 2.
 * <p>
 * Modelo escolhido: sessão em cookie HttpOnly + CSRF no formato que o
 * {@code HttpClient} do Angular já entende (cookie {@code XSRF-TOKEN} lido pelo
 * front e devolvido no header {@code X-XSRF-TOKEN}).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated())
            .csrf(csrf -> csrf.spa())
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            // API REST: sem redirecionar para página de login, só 401.
            .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }

    /**
     * Encoder delegante: grava {@code {bcrypt}...} e permite trocar o algoritmo no
     * futuro sem invalidar as senhas existentes. No legado as senhas eram texto puro.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
