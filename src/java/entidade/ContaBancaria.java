
package entidade;

/**
 *
 * @author lucas
 * 
 */
public class ContaBancaria {
    
    private int contaCorrente;
    private int contaPoupanca;
    private int idCliente;
    private float saldoAtual;
    private int idAgencia;
    
    public ContaBancaria() {
    }

    public ContaBancaria(int contaCorrente, int contaPoupanca, int idCliente, float saldoAtual, int idAgencia) {
        this.contaCorrente = contaCorrente;
        this.contaPoupanca = contaPoupanca;
        this.idCliente = idCliente;
        this.saldoAtual = saldoAtual;
        this.idAgencia = idAgencia;
    }

    public void setContaCorrente(int contaCorrente) {
        this.contaCorrente = contaCorrente;
    }

    public void setContaPoupanca(int contaPoupanca) {
        this.contaPoupanca = contaPoupanca;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public void setSaldoAtual(float saldoAtual) {
        this.saldoAtual = saldoAtual;
    }

    public void setIdAgencia(int idAgencia) {
        this.idAgencia = idAgencia;
    }

    public int getContaCorrente() {
        return contaCorrente;
    }

    public int getContaPoupanca() {
        return contaPoupanca;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public float getSaldoAtual() {
        return saldoAtual;
    }

    public int getIdAgencia() {
        return idAgencia;
    }

}
