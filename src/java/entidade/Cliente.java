package entidade;

public class Cliente {

    private int id;
    private int id_usuario;
    private String tipo_cliente;
    
    public Cliente() {
        this.id_usuario = 0;
        this.tipo_cliente = "";
    }

    public Cliente(int id_usuario, String tipo_cliente) {
        this.id_usuario = id_usuario;
        this.tipo_cliente = tipo_cliente;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public int getId_usuario() {
        return id_usuario;
    }
    
    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    public void setTipo_Cliente(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getTipo_Cliente() {
        return tipo_cliente;
    }

    public void setTipo_Cliente(String tipo_cliente) {
        this.tipo_cliente = tipo_cliente;
    }
}
