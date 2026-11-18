package br.uff.banco.identidade.application;

import br.uff.banco.identidade.domain.RefreshToken;
import br.uff.banco.identidade.domain.Usuario;
import br.uff.banco.identidade.infra.EmissorDeTokens;
import br.uff.banco.identidade.infra.RefreshTokenRepository;
import br.uff.banco.identidade.infra.UsuarioRepository;
import br.uff.banco.shared.domain.Cpf;
import br.uff.banco.shared.domain.NaoAutenticadoException;
import br.uff.banco.shared.domain.RecursoNaoEncontradoException;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Login, renovação e logout.
 * <p>
 * O access token (JWT) é stateless e vale até expirar; por isso é curto. O que dá
 * controle sobre a sessão é o refresh token: rotacionado a cada uso, revogado no
 * logout, e com detecção de reuso (token já usado apresentado de novo = possível
 * roubo → a família inteira é revogada).
 */
@Service
public class AutenticacaoService {

    private static final Logger log = LoggerFactory.getLogger(AutenticacaoService.class);
    private static final String CREDENCIAIS_INVALIDAS = "CPF ou senha inválidos.";
    private static final String SESSAO_INVALIDA = "Sessão inválida ou expirada. Faça login novamente.";

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarios;
    private final RefreshTokenRepository refreshTokens;
    private final EmissorDeTokens emissor;
    private final Clock relogio;

    AutenticacaoService(AuthenticationManager authenticationManager, UsuarioRepository usuarios,
            RefreshTokenRepository refreshTokens, EmissorDeTokens emissor, Clock relogio) {
        this.authenticationManager = authenticationManager;
        this.usuarios = usuarios;
        this.refreshTokens = refreshTokens;
        this.emissor = emissor;
        this.relogio = relogio;
    }

    @Transactional
    public TokensView login(String cpf, String senha) {
        Authentication autenticacao;
        try {
            autenticacao = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(cpf, senha));
        } catch (AuthenticationException e) {
            // Senha errada, CPF inexistente/malformado e usuário inativo: mesma resposta.
            throw new NaoAutenticadoException(CREDENCIAIS_INVALIDAS);
        }
        Usuario usuario = usuarios.findByCpf(new Cpf(autenticacao.getName()))
                .orElseThrow(() -> new NaoAutenticadoException(CREDENCIAIS_INVALIDAS));
        return emitir(usuario, UUID.randomUUID().toString());
    }

    /**
     * Troca um refresh token válido por um novo par. O token usado é revogado.
     * <p>
     * {@code noRollbackFor}: quando detecta reuso, a revogação da família precisa
     * ser gravada mesmo com a requisição terminando em 401.
     */
    @Transactional(noRollbackFor = NaoAutenticadoException.class)
    public TokensView renovar(String refreshToken) {
        Instant agora = relogio.instant();
        RefreshToken atual = refreshTokens.findParaRotacao(EmissorDeTokens.hash(refreshToken))
                .orElseThrow(() -> new NaoAutenticadoException(SESSAO_INVALIDA));

        if (atual.isRevogado()) {
            int revogados = refreshTokens.revogarFamilia(atual.getFamilia(), agora);
            log.warn("Reuso de refresh token detectado (usuário {}); {} token(s) da família revogado(s).",
                    atual.getUsuario().getId(), revogados);
            throw new NaoAutenticadoException(SESSAO_INVALIDA);
        }
        if (atual.isExpirado(agora) || !atual.getUsuario().isAtivo()) {
            atual.revogar(agora);
            throw new NaoAutenticadoException(SESSAO_INVALIDA);
        }

        atual.revogar(agora);
        Usuario usuario = usuarios.findComPapeisById(atual.getUsuario().getId())
                .orElseThrow(() -> new NaoAutenticadoException(SESSAO_INVALIDA));
        return emitir(usuario, atual.getFamilia());
    }

    /** Encerra a sessão revogando a família do refresh token. Idempotente: token desconhecido não é erro. */
    @Transactional
    public void logout(String refreshToken) {
        refreshTokens.findByTokenHash(EmissorDeTokens.hash(refreshToken))
                .ifPresent(token -> refreshTokens.revogarFamilia(token.getFamilia(), relogio.instant()));
    }

    @Transactional(readOnly = true)
    public UsuarioView usuarioAtual(Long id) {
        return usuarios.findComPapeisById(id)
                .map(UsuarioView::de)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Usuário", id));
    }

    private TokensView emitir(Usuario usuario, String familia) {
        Instant agora = relogio.instant();
        String refreshToken = emissor.novoRefreshToken();
        refreshTokens.save(new RefreshToken(usuario, EmissorDeTokens.hash(refreshToken), familia, agora,
                agora.plus(emissor.propriedades().validadeRefreshToken())));
        return TokensView.bearer(emissor.accessToken(usuario, agora), emissor.segundosDeValidadeAccessToken(), refreshToken);
    }
}
