package br.uff.banco.shared.persistence;

import br.uff.banco.shared.domain.Cpf;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Mapeia {@link Cpf} para colunas {@code CHAR(11)} (sem máscara). */
@Converter(autoApply = true)
public class CpfConverter implements AttributeConverter<Cpf, String> {

    @Override
    public String convertToDatabaseColumn(Cpf cpf) {
        return cpf == null ? null : cpf.numero();
    }

    @Override
    public Cpf convertToEntityAttribute(String numero) {
        return numero == null ? null : new Cpf(numero);
    }
}
