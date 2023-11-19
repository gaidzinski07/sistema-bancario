package entidade;

/* @author lucas */

public class Fundo {

    private int idFundo;
    private float valorCota;
    private float valorMinimo;
    
    public Fundo() {
    }

    public Fundo(int idFundo, float valorCota, float valorMinimo) {
        this.idFundo = idFundo;
        this.valorCota = valorCota;
        this.valorMinimo = valorMinimo;
    }

    public void setValorCota(float valorCota) {
        this.valorCota = valorCota;
    }

    public void setValorMinimo(float valorMinimo) {
        this.valorMinimo = valorMinimo;
    }

    public void setIdFundo(int idFundo) {
        this.idFundo = idFundo;
    }

    public int getIdFundo() {
        return idFundo;
    }

    public float getValorCota() {
        return valorCota;
    }

    public float getValorMinimo() {
        return valorMinimo;
    }

}
