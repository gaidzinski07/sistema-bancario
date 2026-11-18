package br.uff.banco.contas.application;

import br.uff.banco.agencias.application.AgenciaService;
import br.uff.banco.agencias.domain.Agencia;
import br.uff.banco.clientes.application.ClienteService;
import br.uff.banco.clientes.domain.Cliente;
import br.uff.banco.contas.domain.Conta;
import br.uff.banco.contas.domain.TipoConta;
import br.uff.banco.contas.infra.ContaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Abertura de contas pelo administrador (no legado, a tela existia mas não estava implementada). */
@Service
public class AberturaContaService {

    private final ContaRepository contas;
    private final ClienteService clientes;
    private final AgenciaService agencias;

    AberturaContaService(ContaRepository contas, ClienteService clientes, AgenciaService agencias) {
        this.contas = contas;
        this.clientes = clientes;
        this.agencias = agencias;
    }

    @Transactional
    public ContaView abrir(Long clienteId, Long agenciaId, TipoConta tipo) {
        Cliente titular = clientes.obter(clienteId);
        // A agência fica travada até o commit: aberturas simultâneas nela esperam a vez.
        Agencia agencia = agencias.travarParaAberturaDeConta(agenciaId);
        Conta conta = Conta.abrir(agencia, agencia.reservarNumeroConta(), titular, tipo);
        return ContaView.de(contas.save(conta));
    }

    @Transactional(readOnly = true)
    public List<ContaView> listarDoCliente(Long clienteId) {
        clientes.obter(clienteId); // 404 se o cliente não existir, em vez de lista vazia
        return contas.findByTitularIdOrderById(clienteId).stream().map(ContaView::de).toList();
    }
}
