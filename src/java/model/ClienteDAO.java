package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import entidade.Cliente;

/*
--
-- Estrutura da tabela `Administrador`
--

CREATE TABLE IF NOT EXISTS `Cliente` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_usuario` int NOT NULL,
  `tipo_cliente` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_user_cli` (`id_usuario`)
  ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


 */
public class ClienteDAO implements Dao<Cliente> {

    @Override
    public Cliente get(int id_user) {
        Conexao conexao = new Conexao();
        Cliente cliente = new Cliente();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM Cliente WHERE ID_USUARIO = ? ");
            sql.setInt(1, id_user);
            ResultSet resultado = sql.executeQuery();

            if (resultado != null) {
                while (resultado.next()) {
                    cliente.setId_usuario(Integer.parseInt(resultado.getString("id_usuario")));
                    cliente.setTipo_Cliente(String.valueOf(resultado.getString("tipo_cliente")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Query de select (get categoria) incorreta");
        } finally {
            conexao.closeConexao();
        }
        return cliente;
    }

    @Override
    public void insert(Cliente t) {

        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("INSERT INTO Cliente (id_usuario, tipo_cliente) VALUES (?,?)");
            sql.setString(1, String.valueOf(t.getId_usuario()));
            sql.setString(2, String.valueOf(t.getTipo_Cliente()));
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Query de insert (categoria) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public void update(Cliente t) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("UPDATE Administrador SET id_usuario = ?, tipo_administrador = ?  WHERE ID_USUARIO = ? ");
            sql.setInt(1, (t.getId_usuario()));
            sql.setString(2, (t.getTipo_Cliente()));                       
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
            PreparedStatement sql = conexao.getConexao().prepareStatement("DELETE FROM Cliente WHERE ID_USUARIO = ? ");
            sql.setInt(1, id_user);
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Query de delete (excluir contasusuario) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public ArrayList<Cliente> getAll() {

        ArrayList<Cliente> meusclientes = new ArrayList();
        Conexao conexao = new Conexao();
        try {
            String selectSQL = "SELECT * FROM Administrador";
            PreparedStatement preparedStatement;
            preparedStatement = conexao.getConexao().prepareStatement(selectSQL);
            ResultSet resultado = preparedStatement.executeQuery();
            if (resultado != null) {
                while (resultado.next()) {
                    Cliente cliente = new Cliente(
                            Integer.parseInt(resultado.getString("id_usuario")),
                            String.valueOf(resultado.getString("tipo_cliente"))
                    );
                    //administrador.setId(Integer.parseInt(resultado.getString("id")));
                    meusclientes.add(cliente);
                }
            }
        } catch (SQLException e) {
            System.err.println("Query de select (GetAll - ContasUsuario) incorreta");
        } finally {
            conexao.closeConexao();
        }
        return meusclientes;
    }
}
