package br.uff.banco.clientes.application;

import br.uff.banco.clientes.domain.Cliente;
import br.uff.banco.clientes.domain.SegmentoCliente;
import br.uff.banco.clientes.infra.ClienteRepository;
import br.uff.banco.identidade.application.CadastroUsuarioService;
import br.uff.banco.identidade.application.NovoUsuario;
import br.uff.banco.identidade.domain.Usuario;
import br.uff.banco.shared.domain.Endereco;
import br.uff.banco.shared.domain.RecursoNaoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private final ClienteRepository clientes;
    private final CadastroUsuarioService cadastroUsuario;

    ClienteService(ClienteRepository clientes, CadastroUsuarioService cadastroUsuario) {
        this.clientes = clientes;
        this.cadastroUsuario = cadastroUsuario;
    }

    /** Cria usuário + cliente numa única transação (no legado eram INSERTs soltos, e o de cliente nem tinha a PK). */
    @Transactional
    public ClienteView cadastrar(NovoUsuario dadosPessoais, SegmentoCliente segmento, Endereco endereco) {
        Usuario usuario = cadastroUsuario.criar(dadosPessoais);
        // saveAndFlush: o ID vem do usuário (@MapsId), então o INSERT ficaria para o commit
        // e a resposta sairia sem criadoEm; o flush também antecipa erros de constraint.
        Cliente cliente = clientes.saveAndFlush(Cliente.de(usuario, segmento, endereco));
        return ClienteView.de(cliente);
    }

    @Transactional(readOnly = true)
    public Page<ClienteView> listar(Pageable paginacao) {
        return clientes.findAll(paginacao).map(ClienteView::de);
    }

    @Transactional(readOnly = true)
    public ClienteView buscar(Long id) {
        return ClienteView.de(obter(id));
    }

    /** Entidade para outros módulos (ex.: abertura de conta), dentro da transação de quem chama. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Cliente obter(Long id) {
        return clientes.findComUsuarioById(id).orElseThrow(() -> RecursoNaoEncontradoException.de("Cliente", id));
    }
}
