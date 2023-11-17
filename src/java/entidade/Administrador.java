package entidade;

public class Administrador {

    private int id_usuario;
    private String tipo_administrador;
    

    public Administrador() {
        this.id_usuario = 0;
        this.tipo_administrador="";
    }

    public Administrador(int id_usuario, String tipo_administrador) {
        this.id_usuario=id_usuario;
        this.tipo_administrador=tipo_administrador;
    }

    public int getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getTipo_Admin() {
        return tipo_administrador;
    }

    public void setTipo_Admin(String tipo_administrador) {
        this.tipo_administrador = tipo_administrador;
    }
}
    
  