package br.uff.banco.identidade.application;

import br.uff.banco.identidade.domain.Usuario;
import br.uff.banco.identidade.infra.UsuarioRepository;
import br.uff.banco.shared.domain.RecursoNaoEncontradoException;
import br.uff.banco.shared.domain.RegraNegocioException;
import java.time.Clock;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Senha de transação: confirmação exigida em operações que movimentam dinheiro
 * (no legado, a tela de transferência exibia a senha digitada na própria página).
 */
@Service
public class SenhaTransacaoService {

    private static final String FORMATO = "\\d{4,6}";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final Clock relogio;

    SenhaTransacaoService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, Clock relogio) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.relogio = relogio;
    }

    /**
     * Confere a senha de transação.
     * <p>
     * Roda numa transação própria ({@code REQUIRES_NEW}) e não desfaz o que gravou ao
     * falhar ({@code noRollbackFor}): o contador de tentativas erradas precisa ficar
     * gravado mesmo que a operação financeira seja recusada.
     * <p>
     * Chame <em>fora</em> de transação e antes de travar as contas. Dentro de outra
     * transação continuaria correto (por causa do REQUIRES_NEW), mas cada chamada
     * seguraria duas conexões do pool ao mesmo tempo.
     *
     * @throws RegraNegocioException senha não definida, bloqueada ou incorreta
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = RegraNegocioException.class)
    public void verificar(Long usuarioId, String senha) {
        Instant agora = relogio.instant();
        Usuario usuario = usuarios.findParaAtualizacao(usuarioId)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Usuário", usuarioId));
        usuario.exigirSenhaTransacaoDisponivel(agora);
        boolean correta = senha != null && passwordEncoder.matches(senha, usuario.getSenhaTransacaoHash());
        usuario.registrarTentativaSenhaTransacao(correta, agora);
    }

    /** Define ou troca a senha de transação. Exige a senha de login para confirmar a identidade. */
    @Transactional
    public void definir(Long usuarioId, String senhaLogin, String novaSenhaTransacao) {
        Usuario usuario = usuarios.findParaAtualizacao(usuarioId)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Usuário", usuarioId));
        if (senhaLogin == null || !passwordEncoder.matches(senhaLogin, usuario.getSenhaHash())) {
            throw new RegraNegocioException("senha-login-incorreta", "Senha de login incorreta.");
        }
        if (novaSenhaTransacao == null || !novaSenhaTransacao.matches(FORMATO)) {
            throw new RegraNegocioException("senha-transacao-formato", "A senha de transação deve ter de 4 a 6 dígitos.");
        }
        usuario.definirSenhaTransacao(passwordEncoder.encode(novaSenhaTransacao));
    }
}
