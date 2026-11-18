package br.uff.banco.shared.web;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valor de operação: obrigatório, maior que zero e com no máximo 2 casas decimais.
 * Valores como {@code 10.005} são recusados em vez de arredondados em silêncio.
 */
@NotNull
@Positive
@Digits(integer = 17, fraction = 2)
@ReportAsSingleViolation
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValorMonetario {

    String message() default "deve ser um valor positivo com até 2 casas decimais";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
