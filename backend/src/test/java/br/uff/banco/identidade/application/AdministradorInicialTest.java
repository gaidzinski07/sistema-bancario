package br.uff.banco.identidade.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.banco.identidade.domain.Papel;
import br.uff.banco.identidade.domain.PerfilAdministrador;
import br.uff.banco.identidade.infra.UsuarioRepository;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdministradorInicialTest {

    UsuarioRepository usuarios;
    CadastroUsuarioService cadastro;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepository.class);
        cadastro = mock(CadastroUsuarioService.class);
        when(cadastro.criarAdministrador(any(), any())).thenReturn(
                new UsuarioView(1L, "Admin", "529.982.247-25", null, null, Set.of(Papel.ADMIN), PerfilAdministrador.GERENTE, true));
    }

    @Test
    void semConfiguracaoNaoFazNada() {
        executar(new AdministradorInicial.Propriedades(null, null, null));

        verify(usuarios, never()).existeComPapel(any());
        verify(cadastro, never()).criarAdministrador(any(), any());
    }

    @Test
    void criaOPrimeiroAdministradorComoGerente() {
        when(usuarios.existeComPapel(Papel.ADMIN)).thenReturn(false);

        executar(new AdministradorInicial.Propriedades("529.982.247-25", "Gerente Inicial", "SenhaForte1"));

        verify(cadastro).criarAdministrador(
                eq(new NovoUsuario("Gerente Inicial", "529.982.247-25", null, null, "SenhaForte1")), eq(PerfilAdministrador.GERENTE));
    }

    @Test
    void naoCriaOutroSeJaExisteAdministrador() {
        when(usuarios.existeComPapel(Papel.ADMIN)).thenReturn(true);

        executar(new AdministradorInicial.Propriedades("529.982.247-25", null, "SenhaForte1"));

        verify(cadastro, never()).criarAdministrador(any(), any());
    }

    @Test
    void cpfSemSenhaValidaImpedeASubida() {
        var propriedades = new AdministradorInicial.Propriedades("529.982.247-25", null, "curta");

        assertThatThrownBy(() -> executar(propriedades))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BANCO_ADMIN_INICIAL_SENHA");
    }

    private void executar(AdministradorInicial.Propriedades propriedades) {
        new AdministradorInicial(propriedades, usuarios, cadastro).run(null);
    }
}
