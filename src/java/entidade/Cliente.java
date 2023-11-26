package entidade;

public class Cliente {

    private int idUsuario;
    private String tipoCliente;
    private int contaBancaria;

    public Cliente() {
    }

    public Cliente(int idUsuario, String tipoCliente, int contaBancaria) {
        this.idUsuario = idUsuario;
        this.tipoCliente = tipoCliente;
        this.contaBancaria = contaBancaria;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    public int getContaBancaria() {
        return contaBancaria;
    }

    public void setContaBancaria(int contaBancaria) {
        this.contaBancaria = contaBancaria;
    }
}
