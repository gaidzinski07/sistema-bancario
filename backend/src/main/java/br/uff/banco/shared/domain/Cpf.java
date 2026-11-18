package br.uff.banco.shared.domain;

import java.util.Objects;

/**
 * CPF validado (11 dígitos + dígitos verificadores), armazenado sem máscara.
 * <p>
 * No legado o CPF era um {@code varchar} livre ('111', '222'...), sem validação.
 */
public record Cpf(String numero) {

    public Cpf {
        Objects.requireNonNull(numero, "numero");
        if (!isValido(numero)) {
            throw new RegraNegocioException("cpf-invalido", "CPF inválido.");
        }
    }

    /** Aceita com ou sem máscara ({@code 529.982.247-25} ou {@code 52998224725}). */
    public static Cpf of(String valor) {
        Objects.requireNonNull(valor, "valor");
        return new Cpf(valor.replaceAll("[.\\-\\s]", ""));
    }

    public String formatado() {
        return numero.substring(0, 3) + "." + numero.substring(3, 6) + "."
                + numero.substring(6, 9) + "-" + numero.substring(9);
    }

    static boolean isValido(String numero) {
        if (numero.length() != 11 || !numero.chars().allMatch(Character::isDigit)) {
            return false;
        }
        if (numero.chars().distinct().count() == 1) {
            return false; // 000.000.000-00, 111.111.111-11 etc. passam no cálculo mas são inválidos
        }
        return digitoVerificador(numero, 9) == numero.charAt(9) - '0'
                && digitoVerificador(numero, 10) == numero.charAt(10) - '0';
    }

    private static int digitoVerificador(String numero, int quantidadeDigitos) {
        int soma = 0;
        for (int i = 0; i < quantidadeDigitos; i++) {
            soma += (numero.charAt(i) - '0') * (quantidadeDigitos + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    @Override
    public String toString() {
        // Evita vazar o documento completo em logs.
        return "***." + numero.substring(3, 6) + "." + numero.substring(6, 9) + "-**";
    }
}
