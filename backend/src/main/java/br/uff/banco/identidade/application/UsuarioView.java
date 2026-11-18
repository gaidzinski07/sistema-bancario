package br.uff.banco.identidade.application;

import br.uff.banco.identidade.domain.Papel;
import br.uff.banco.identidade.domain.PerfilAdministrador;
import br.uff.banco.identidade.domain.Usuario;
import java.time.LocalDate;
import java.util.Set;

public record UsuarioView(
        Long id,
        String nome,
        String cpf,
        String email,
        LocalDate dataNascimento,
        Set<Papel> papeis,
        PerfilAdministrador perfilAdministrador,
        boolean ativo) {

    public static UsuarioView de(Usuario usuario) {
        return new UsuarioView(
                usuario.getId(),
                usuario.getNome(),
                usuario.getCpf().formatado(),
                usuario.getEmail(),
                usuario.getDataNascimento(),
                usuario.getPapeis(),
                usuario.getPerfilAdministrador(),
                usuario.isAtivo());
    }
}
