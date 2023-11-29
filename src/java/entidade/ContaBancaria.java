
package entidade;

/**
 *
 * @author lucas
 * 
 */
public class ContaBancaria {
    
    private int contaCorrente;
    private int idAgencia;
    private float saldoAtual;

    public ContaBancaria() {
    }

    public ContaBancaria(int contaCorrente, int idAgencia, float saldoAtual) {
        this.contaCorrente = contaCorrente;
        this.idAgencia = idAgencia;
        this.saldoAtual = saldoAtual;
    }
    
    public int getContaCorrente() {
        return contaCorrente;
    }

    public void setContaCorrente(int contaCorrente) {
        this.contaCorrente = contaCorrente;
    }

    public int getIdAgencia() {
        return idAgencia;
    }

    public void setIdAgencia(int idAgencia) {
        this.idAgencia = idAgencia;
    }

    public float getSaldoAtual() {
        return saldoAtual;
    }

    public void setSaldoAtual(float saldoAtual) {
        this.saldoAtual = saldoAtual;
    }

}
