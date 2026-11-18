package br.uff.banco.suporte;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Aplicação completa + MySQL do Testcontainers (com migrations e seed de dev).
 * <p>
 * Todas as classes anotadas compartilham o mesmo contexto e o mesmo banco: os
 * testes devem criar seus próprios dados (ex.: {@link Cpfs#novo()}) em vez de
 * depender de contagens globais.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Import(MySqlTestcontainersConfig.class)
@EnabledIf(value = "br.uff.banco.suporte.Docker#disponivel", disabledReason = "Docker indisponível")
public @interface TesteDeIntegracao {
}
