package br.uff.banco.shared.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Segurança da API: autenticação stateless por JWT (Bearer).
 * <ul>
 *   <li>{@code POST /api/auth/login} troca CPF + senha por um access token (JWT, curto) e um refresh token (opaco, revogável).</li>
 *   <li>Toda outra chamada envia {@code Authorization: Bearer <accessToken>}.</li>
 *   <li>Sem sessão e sem cookies, portanto sem CSRF: o navegador não envia o token sozinho.</li>
 *   <li>{@code /api/admin/**} exige o papel ADMIN; contas e transferências do cliente exigem CLIENTE.</li>
 *   <li>O restante de {@code /api/**} exige autenticação; fora da API ficam só os arquivos do frontend.</li>
 * </ul>
 * O JWT é assinado com HMAC-SHA256 (chave simétrica de {@code JWT_SECRET}). Se outro
 * serviço precisar validar tokens sem conhecer a chave, o caminho é migrar para RS256.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private static final String[] ROTAS_PUBLICAS_POST = {"/api/auth/login", "/api/auth/refresh", "/api/auth/logout"};

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) throws Exception {
        // 401 e 403 saem como ProblemDetail, pelo mesmo ApiExceptionHandler dos demais erros.
        AuthenticationEntryPoint naoAutenticado = (request, response, ex) -> resolver.resolveException(request, response, null, ex);
        AccessDeniedHandler acessoNegado = (request, response, ex) -> resolver.resolveException(request, response, null, ex);

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.POST, ROTAS_PUBLICAS_POST).permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/contas/**", "/api/transferencias/**", "/api/investimentos/**").hasRole("CLIENTE")
                .requestMatchers("/api/**", "/actuator/**").authenticated()
                // Fora da API: arquivos e rotas do frontend (FrontendConfig), públicos por natureza.
                .anyRequest().permitAll())
            .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable())
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorDePapeis()))
                .authenticationEntryPoint(naoAutenticado)
                .accessDeniedHandler(acessoNegado))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(naoAutenticado)
                .accessDeniedHandler(acessoNegado));
        return http.build();
    }

    /** Claim {@code papeis: ["ADMIN", "CLIENTE"]} → authorities {@code ROLE_ADMIN}, {@code ROLE_CLIENTE}. */
    private static JwtAuthenticationConverter conversorDePapeis() {
        var papeis = new JwtGrantedAuthoritiesConverter();
        papeis.setAuthoritiesClaimName(UsuarioAutenticado.CLAIM_PAPEIS);
        papeis.setAuthorityPrefix("ROLE_");
        var conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(papeis);
        return conversor;
    }

    @Bean
    JwtEncoder jwtEncoder(JwtProperties propriedades) {
        return NimbusJwtEncoder.withSecretKey(propriedades.chave()).algorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    JwtDecoder jwtDecoder(JwtProperties propriedades) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(propriedades.chave())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        // Valida exp/nbf (com tolerância padrão de 60s) e o emissor.
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(propriedades.emissor()));
        return decoder;
    }

    /** Autenticação por CPF + senha, usada só no login. */
    @Bean
    AuthenticationManager authenticationManager(UserDetailsService usuarios, PasswordEncoder passwordEncoder) {
        var provider = new DaoAuthenticationProvider(usuarios);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
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
