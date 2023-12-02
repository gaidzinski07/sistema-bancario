
package entidade;

/**
 *
 * @author lucas
 * 
 */
public class ContaBancaria {
    
    private int contaCorrente;
    //private int contaPoupança;
    private int idAgencia;
    private float saldoAtual;
    private float saldoAtualPP;

    //public ContaBancaria() {
    //}

    public ContaBancaria(int contaCorrente, int idAgencia, float saldoAtual, float saldoAtualPP) {
        this.contaCorrente = contaCorrente;
        this.idAgencia = idAgencia;
        this.saldoAtual = saldoAtual;
        this.saldoAtualPP = saldoAtualPP;
    }
    
    public ContaBancaria(){
        this.contaCorrente = 0;
        this.idAgencia = 0;
        this.saldoAtual = 0;
        this.saldoAtualPP = 0;
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
    
    public float getSaldoAtualPP() {
        return saldoAtualPP;
    }

    public void setSaldoAtualPP(float saldoAtualPP) {
        this.saldoAtualPP = saldoAtualPP;
    }

}
