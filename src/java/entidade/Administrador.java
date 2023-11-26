package entidade;

public class Administrador {

    private int idUsuario;
    private String tipoAdministrador;

    public Administrador() {
    }

    public Administrador(int idUsuario, String tipoAdministrador) {
        this.idUsuario = idUsuario;
        this.tipoAdministrador = tipoAdministrador;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getTipoAdministrador() {
        return tipoAdministrador;
    }

    public void setTipoAdministrador(String tipoAdministrador) {
        this.tipoAdministrador = tipoAdministrador;
    }
}
    
  