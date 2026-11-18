package br.uff.banco.contas.application;

import br.uff.banco.contas.domain.Conta;
import br.uff.banco.contas.domain.Lancamento;
import br.uff.banco.contas.domain.TipoLancamento;
import br.uff.banco.contas.domain.Transferencia;
import br.uff.banco.contas.infra.ContaRepository;
import br.uff.banco.contas.infra.LancamentoRepository;
import br.uff.banco.contas.infra.TransferenciaRepository;
import br.uff.banco.identidade.application.SenhaTransacaoService;
import br.uff.banco.shared.domain.Cpf;
import br.uff.banco.shared.domain.RecursoNaoEncontradoException;
import br.uff.banco.shared.domain.RegraNegocioException;
import java.time.Clock;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Transferência entre contas do banco.
 * <p>
 * Corrige o que o {@code Realizartransferencia} do legado fazia: origem fixa na
 * conta 1, sem conferir saldo nem sinal do valor, agência e CPF do destino lidos e
 * ignorados, e débito/crédito/registro em commits separados.
 */
@Service
public class TransferenciaService {

    private static final String DESTINO_NAO_ENCONTRADO = "Conta de destino não encontrada para os dados informados.";

    private final ContaRepository contas;
    private final ContasTravadas contasTravadas;
    private final TransferenciaRepository transferencias;
    private final LancamentoRepository lancamentos;
    private final SenhaTransacaoService senhaTransacao;
    private final TransactionTemplate leitura;
    private final TransactionTemplate escrita;
    private final Clock relogio;

    TransferenciaService(ContaRepository contas, ContasTravadas contasTravadas, TransferenciaRepository transferencias,
            LancamentoRepository lancamentos, SenhaTransacaoService senhaTransacao, PlatformTransactionManager transacoes,
            Clock relogio) {
        this.contas = contas;
        this.contasTravadas = contasTravadas;
        this.transferencias = transferencias;
        this.lancamentos = lancamentos;
        this.senhaTransacao = senhaTransacao;
        this.leitura = new TransactionTemplate(transacoes);
        this.leitura.setReadOnly(true);
        this.escrita = new TransactionTemplate(transacoes);
        this.relogio = relogio;
    }

    /**
     * Executa a transferência — ou, se a {@code chaveIdempotencia} já foi usada pelo
     * mesmo cliente com os mesmos dados, devolve o comprovante original sem mover
     * dinheiro de novo.
     * <p>
     * Dois envios simultâneos com a mesma chave: o segundo espera o lock da conta de
     * origem e, ao inserir, esbarra na chave única (409); só um débito acontece.
     * <p>
     * A senha de transação é conferida antes, em transação própria (ver
     * {@link OperacaoContaService} sobre não aninhar transações).
     */
    public ResultadoTransferencia transferir(Long clienteId, NovaTransferencia pedido) {
        Optional<ComprovanteTransferencia> reenvio = leitura.execute(status ->
                transferencias.findByChaveIdempotencia(pedido.chaveIdempotencia())
                        .map(anterior -> reenvio(anterior, clienteId, pedido)));
        if (reenvio.isPresent()) {
            return new ResultadoTransferencia(reenvio.get(), false);
        }
        senhaTransacao.verificar(clienteId, pedido.senhaTransacao());
        return escrita.execute(status -> new ResultadoTransferencia(executar(clienteId, pedido), true));
    }

    private ComprovanteTransferencia executar(Long clienteId, NovaTransferencia pedido) {
        NumeroConta destinoInformado = NumeroConta.parse(pedido.contaDestino());
        Long destinoId = contas.findIdPorAgenciaENumero(pedido.agenciaDestino(), destinoInformado.numero())
                .orElseThrow(() -> new RegraNegocioException("destino-nao-encontrado", DESTINO_NAO_ENCONTRADO));

        ContasTravadas.Par par = contasTravadas.par(pedido.contaOrigemId(), destinoId);
        Conta origem = par.origem();
        Conta destino = par.destino();
        if (!origem.pertenceA(clienteId)) {
            throw RecursoNaoEncontradoException.de("Conta", pedido.contaOrigemId());
        }
        exigirDestinoConfere(destino, destinoInformado, pedido.cpfDestino());

        Transferencia transferencia = transferencias.save(
                Transferencia.entre(origem, destino, pedido.valor(), pedido.chaveIdempotencia(), relogio.instant()));
        Lancamento saida = lancamentos.save(origem.debitar(transferencia.movimentoDeSaida(descrever(destino))));
        lancamentos.save(destino.creditar(transferencia.movimentoDeEntrada(descrever(origem))));

        return ComprovanteTransferencia.de(transferencia, saida.getSaldoApos());
    }

    private ComprovanteTransferencia reenvio(Transferencia anterior, Long clienteId, NovaTransferencia pedido) {
        boolean mesmoPedido = anterior.getOrigem().pertenceA(clienteId)
                && anterior.getOrigem().getId().equals(pedido.contaOrigemId())
                && anterior.getValor().equals(pedido.valor())
                && anterior.getDestino().getAgencia().getCodigo().equals(pedido.agenciaDestino())
                && anterior.getDestino().getNumeroComDigito().equals(pedido.contaDestino());
        if (!mesmoPedido) {
            throw new RegraNegocioException("idempotencia-conflitante",
                    "Esta chave de idempotência já foi usada em outra transferência.");
        }
        Lancamento saida = lancamentos.findByTipoAndOperacaoId(TipoLancamento.TRANSFERENCIA_ENVIADA, anterior.getId())
                .orElseThrow(() -> new IllegalStateException("Transferência " + anterior.getId() + " sem lançamento de saída"));
        return ComprovanteTransferencia.de(anterior, saida.getSaldoApos());
    }

    /** Dígito e CPF do titular têm de bater. Erro genérico para não confirmar quais dados estavam certos. */
    private static void exigirDestinoConfere(Conta destino, NumeroConta informado, String cpfInformado) {
        boolean digitoConfere = destino.getDigito().equals(informado.digito());
        boolean cpfConfere;
        try {
            cpfConfere = destino.getTitular().getUsuario().getCpf().equals(Cpf.of(cpfInformado));
        } catch (RegraNegocioException cpfMalformado) {
            cpfConfere = false;
        }
        if (!digitoConfere || !cpfConfere) {
            throw new RegraNegocioException("destino-nao-encontrado", DESTINO_NAO_ENCONTRADO);
        }
    }

    private static String descrever(Conta conta) {
        return conta.getTitular().getUsuario().getNome()
                + " — ag " + conta.getAgencia().getCodigo() + " cc " + conta.getNumeroComDigito();
    }

    /** Número de conta no formato {@code 00003-5}. */
    record NumeroConta(String numero, String digito) {

        static NumeroConta parse(String valor) {
            if (valor == null || !valor.matches("\\d{1,10}-\\d")) {
                throw new RegraNegocioException("conta-destino-formato", "Informe a conta de destino no formato 00000-0.");
            }
            String[] partes = valor.split("-");
            return new NumeroConta(partes[0], partes[1]);
        }
    }

    public record ResultadoTransferencia(ComprovanteTransferencia comprovante, boolean nova) {
    }
}
