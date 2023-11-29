package entidade;

/* @author lucas */

public class Fundo {

    private int id;
    private String nome;
    private float valorCota;
    private float valorMinimo;

    public Fundo() {
    }

    public Fundo(int id, String nome, float valorCota, float valorMinimo) {
        this.id = id;
        this.nome = nome;
        this.valorCota = valorCota;
        this.valorMinimo = valorMinimo;
    }

    public int getId() {
        return id;
    }

    public void setIdFundo(int idFundo) {
        this.id = idFundo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public float getValorCota() {
        return valorCota;
    }

    public void setValorCota(float valorCota) {
        this.valorCota = valorCota;
    }

    public float getValorMinimo() {
        return valorMinimo;
    }

    public void setValorMinimo(float valorMinimo) {
        this.valorMinimo = valorMinimo;
    }
    
}
