package br.uff.banco.identidade.application;

import java.time.LocalDate;

/** Dados para criar um usuário (comando de aplicação; a validação de formato fica na API). */
public record NovoUsuario(String nome, String cpf, String email, LocalDate dataNascimento, String senha) {

    @Override
    public String toString() {
        return "NovoUsuario[nome=" + nome + "]"; // não vaza senha nem CPF em logs
    }
}
