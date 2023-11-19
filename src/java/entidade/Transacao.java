package entidade;

public class Transacao {
    private String id;
    private String doadorFavorecido;
    private String entrada;
    private String saida;

    public Transacao(String id, String doadorFavorecido, String entrada, String saida) {
        this.id = id;
        this.doadorFavorecido = doadorFavorecido;
        this.entrada = entrada;
        this.saida = saida;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return doadorFavorecido;
    }

    public void setDoadorFavorecido(String doadorFavorecido) {
        this.doadorFavorecido = doadorFavorecido;
    }

    public String getEntrada() {
        return entrada;
    }

    public void setEntrada(String entrada) {
        this.entrada = entrada;
    }

    public String getSaida() {
        return saida;
    }

    public void setSaida(String saida) {
        this.saida = saida;
    }
}
