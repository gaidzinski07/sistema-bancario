package br.uff.banco.shared.persistence;

import br.uff.banco.shared.domain.Dinheiro;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.math.BigDecimal;

/** Mapeia {@link Dinheiro} para colunas {@code DECIMAL(19,2)}. */
@Converter(autoApply = true)
public class DinheiroConverter implements AttributeConverter<Dinheiro, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(Dinheiro dinheiro) {
        return dinheiro == null ? null : dinheiro.valor();
    }

    @Override
    public Dinheiro convertToEntityAttribute(BigDecimal valor) {
        return valor == null ? null : Dinheiro.of(valor);
    }
}
