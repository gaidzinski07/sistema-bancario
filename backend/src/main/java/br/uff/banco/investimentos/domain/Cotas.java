package br.uff.banco.investimentos.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Convenções numéricas de cotas (quantidade e valor): 8 casas decimais, como as
 * colunas {@code DECIMAL(19,8)}.
 * <p>
 * Regras de arredondamento (sempre a favor de não criar dinheiro):
 * <ul>
 *   <li>aplicação: cotas = valor ÷ cota, <b>truncado</b> — o cliente nunca recebe mais cotas do que pagou;</li>
 *   <li>resgate parcial: cotas = valor ÷ cota, <b>arredondado para cima</b> — as cotas baixadas sempre cobrem o valor pago;</li>
 *   <li>valor atual: cotas × cota, <b>truncado</b> no centavo — o resgate total paga exatamente o que a carteira mostra.</li>
 * </ul>
 */
public final class Cotas {

    public static final int ESCALA = 8;

    private Cotas() {
    }

    public static BigDecimal normalizar(BigDecimal valor) {
        return valor.setScale(ESCALA, RoundingMode.UNNECESSARY);
    }

    public static boolean positiva(BigDecimal valor) {
        return valor != null && valor.signum() > 0;
    }
}
