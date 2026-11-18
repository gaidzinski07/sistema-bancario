package br.uff.banco.agencias.application;

import br.uff.banco.agencias.domain.Agencia;
import br.uff.banco.agencias.infra.AgenciaRepository;
import br.uff.banco.shared.domain.Endereco;
import br.uff.banco.shared.domain.RecursoNaoEncontradoException;
import br.uff.banco.shared.domain.RegraNegocioException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgenciaService {

    private final AgenciaRepository agencias;

    AgenciaService(AgenciaRepository agencias) {
        this.agencias = agencias;
    }

    @Transactional
    public AgenciaView criar(String codigo, String nome, Endereco endereco) {
        if (agencias.existsByCodigo(codigo)) {
            throw new RegraNegocioException("codigo-agencia-em-uso", "Já existe uma agência com o código " + codigo + ".");
        }
        return AgenciaView.de(agencias.save(new Agencia(codigo, nome, endereco)));
    }

    @Transactional(readOnly = true)
    public List<AgenciaView> listar() {
        return agencias.findAll(Sort.by("codigo")).stream().map(AgenciaView::de).toList();
    }

    /**
     * Agência travada para escrita até o fim da transação de quem chama — usado na
     * abertura de conta para reservar o número.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Agencia travarParaAberturaDeConta(Long id) {
        return agencias.findParaAtualizacao(id).orElseThrow(() -> RecursoNaoEncontradoException.de("Agência", id));
    }
}
