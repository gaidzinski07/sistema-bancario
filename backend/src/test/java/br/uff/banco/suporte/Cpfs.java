package br.uff.banco.suporte;

import java.util.concurrent.ThreadLocalRandom;

/** Gera CPFs válidos (com dígitos verificadores corretos) para dados de teste. */
public final class Cpfs {

    private Cpfs() {
    }

    public static String novo() {
        int[] d = new int[11];
        for (int i = 0; i < 9; i++) {
            d[i] = ThreadLocalRandom.current().nextInt(10);
        }
        d[9] = digito(d, 9);
        d[10] = digito(d, 10);
        var cpf = new StringBuilder();
        for (int digito : d) {
            cpf.append(digito);
        }
        return cpf.chars().distinct().count() == 1 ? novo() : cpf.toString();
    }

    private static int digito(int[] d, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += d[i] * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
