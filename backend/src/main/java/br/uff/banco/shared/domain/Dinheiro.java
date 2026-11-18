package br.uff.banco.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Valor monetário em reais, sempre com 2 casas decimais.
 * <p>
 * Substitui o {@code float} usado no legado. Arredondamento bancário (HALF_EVEN).
 * O sinal não é restringido aqui: quem exige valor positivo (operações) ou
 * não negativo (saldo) é a regra de negócio que usa o valor.
 */
public record Dinheiro(BigDecimal valor) implements Comparable<Dinheiro> {

    public static final int ESCALA = 2;
    public static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_EVEN;
    public static final Dinheiro ZERO = new Dinheiro(BigDecimal.ZERO);

    public Dinheiro {
        Objects.requireNonNull(valor, "valor");
        valor = valor.setScale(ESCALA, ARREDONDAMENTO);
    }

    public static Dinheiro of(BigDecimal valor) {
        return new Dinheiro(valor);
    }

    public static Dinheiro of(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    public Dinheiro somar(Dinheiro outro) {
        return new Dinheiro(valor.add(outro.valor));
    }

    public Dinheiro subtrair(Dinheiro outro) {
        return new Dinheiro(valor.subtract(outro.valor));
    }

    public Dinheiro multiplicar(BigDecimal fator) {
        return new Dinheiro(valor.multiply(fator));
    }

    public boolean isPositivo() {
        return valor.signum() > 0;
    }

    public boolean isNegativo() {
        return valor.signum() < 0;
    }

    public boolean isMenorQue(Dinheiro outro) {
        return compareTo(outro) < 0;
    }

    /** Lança {@link RegraNegocioException} se o valor não for maior que zero. */
    public Dinheiro exigirPositivo() {
        if (!isPositivo()) {
            throw new RegraNegocioException("valor-invalido", "O valor deve ser maior que zero.");
        }
        return this;
    }

    @Override
    public int compareTo(Dinheiro outro) {
        return valor.compareTo(outro.valor);
    }

    @Override
    public String toString() {
        return valor.toPlainString();
    }
}
