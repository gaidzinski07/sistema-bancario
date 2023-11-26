package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import entidade.Administrador;

/*
--
-- Estrutura da tabela `Administrador`
--

CREATE TABLE IF NOT EXISTS `Administrador` (
    `id_usuario` int NOT NULL,
    `tipo_administrador` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  KEY `FK_user_adm` (`id_usuario`)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


 */
public class AdministradorDAO implements Dao<Administrador> {

    @Override
    public Administrador get(int id_user) {
        Conexao conexao = new Conexao();
        Administrador administrador = new Administrador();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM Administrador WHERE ID_USUARIO = ? ");
            sql.setInt(1, id_user);
            ResultSet resultado = sql.executeQuery();

            if (resultado != null) {
                while (resultado.next()) {
                    administrador.setIdUsuario(Integer.parseInt(resultado.getString("id_usuario")));
                    administrador.setTipoAdministrador(String.valueOf(resultado.getString("tipo_administrador")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Query de select (get categoria) incorreta");
        } finally {
            conexao.closeConexao();
        }
        return administrador;
    }

    @Override
    public void insert(Administrador t) {

        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("INSERT INTO Administrador (id_usuario, tipo_administrador) VALUES (?,?)");
            sql.setString(1, String.valueOf(t.getIdUsuario()));
            sql.setString(2, String.valueOf(t.getTipoAdministrador()));
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Query de insert (categoria) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public void update(Administrador t) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("UPDATE Administrador SET id_usuario = ?, tipo_administrador = ?  WHERE ID_USUARIO = ? ");
            sql.setInt(1, (t.getIdUsuario()));
            sql.setString(2, (t.getTipoAdministrador()));                       
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Query de update (alterar categoria) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public void delete(int id_user) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("DELETE FROM Administrador WHERE ID_USUARIO = ? ");
            sql.setInt(1, id_user);
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Query de delete (excluir contasusuario) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public ArrayList<Administrador> getAll() {

        ArrayList<Administrador> meusadmin = new ArrayList();
        Conexao conexao = new Conexao();
        try {
            String selectSQL = "SELECT * FROM Administrador";
            PreparedStatement preparedStatement;
            preparedStatement = conexao.getConexao().prepareStatement(selectSQL);
            ResultSet resultado = preparedStatement.executeQuery();
            if (resultado != null) {
                while (resultado.next()) {
                    Administrador administrador = new Administrador(
                            Integer.parseInt(resultado.getString("id_usuario")),
                            String.valueOf(resultado.getString("tipo_administrador"))
                    );
                    //administrador.setId(Integer.parseInt(resultado.getString("id")));
                    meusadmin.add(administrador);
                }
            }
        } catch (SQLException e) {
            System.err.println("Query de select (GetAll - ContasUsuario) incorreta");
        } finally {
            conexao.closeConexao();
        }
        return meusadmin;
    }
}
