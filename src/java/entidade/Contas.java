package entidade;

public class Contas {

    private int tipo;
    private String descricao;
    
    public Contas() {
        this.tipo = 0;
        this.descricao = "";
        
    }

    public Contas(int tipo, String descricao) {
        this.tipo = tipo;
        this.descricao = descricao;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
