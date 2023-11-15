package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import entidade.Contasusuario;

/*
--
-- Estrutura da tabela `categorias`
--

CREATE TABLE IF NOT EXISTS `categorias` (
  `id` int NOT NULL AUTO_INCREMENT,
  `descricao` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


 */
public class ContasusuarioDAO implements Dao<Contasusuario> {

    @Override
    public Contasusuario get(int id_user) {
        Conexao conexao = new Conexao();
        Contasusuario contasusuario = new Contasusuario();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM Contasusuario WHERE ID_USER = ? ");
            sql.setInt(1, id_user);
            ResultSet resultado = sql.executeQuery();

            if (resultado != null) {
                while (resultado.next()) {
                    contasusuario.setId(Integer.parseInt(resultado.getString("ID")));
                    contasusuario.setId_user(Integer.parseInt(resultado.getString("ID_USER")));
                    contasusuario.settipoconta(Integer.parseInt(resultado.getString("TIPOCONTA")));
                    contasusuario.setagencia(Integer.parseInt(resultado.getString("AGENCIA")));
                    contasusuario.setnumconta(Integer.parseInt(resultado.getString("NUMCONTA")));
                    contasusuario.setsaldo(Float.parseFloat(resultado.getString("SALDO")));            
                }
            }
        } catch (SQLException e) {
            System.err.println("Query de select (get categoria) incorreta");
        } finally {
            conexao.closeConexao();
        }
        return contasusuario;
    }

    @Override
    public void insert(Contasusuario t) {

        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("INSERT INTO Contasusuario (id_user, tipoconta,agencia, conta, saldo) VALUES (?,?,?,?,?)");
            sql.setString(1, String.valueOf(t.getId_user()));
            sql.setString(2, String.valueOf(t.gettipoconta()));
            sql.setString(3, String.valueOf(t.getagencia()));
            sql.setString(4, String.valueOf(t.getnumconta()));
            sql.setString(1, String.valueOf(t.getsaldo()));
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Query de insert (categoria) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public void update(Contasusuario t) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("UPDATE Contasusuario SET id_user = ?, tipoconta = ?, agencia = ?, numconta =?, saldo = ?  WHERE ID = ? ");
            sql.setInt(1, (t.getId_user()));
            sql.setInt(2, (t.gettipoconta()));
            sql.setInt(3, t.getagencia());
            sql.setInt(4, t.getnumconta());
            sql.setFloat(5, (t.getsaldo()));            
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
            PreparedStatement sql = conexao.getConexao().prepareStatement("DELETE FROM Contasusuario WHERE ID_USER = ? ");
            sql.setInt(1, id_user);
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Query de delete (excluir contasusuario) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public ArrayList<Contasusuario> getAll() {

        ArrayList<Contasusuario> minhascontasusuario = new ArrayList();
        Conexao conexao = new Conexao();
        try {
            String selectSQL = "SELECT * FROM Contasusuario";
            PreparedStatement preparedStatement;
            preparedStatement = conexao.getConexao().prepareStatement(selectSQL);
            ResultSet resultado = preparedStatement.executeQuery();
            if (resultado != null) {
                while (resultado.next()) {
                    Contasusuario contasusuario = new Contasusuario(
                            Integer.parseInt(resultado.getString("id_user")),
                            Integer.parseInt(resultado.getString("tipoconta")),
                            Integer.parseInt(resultado.getString("agencia")),
                            Integer.parseInt(resultado.getString("numconta")),
                            Integer.parseInt(resultado.getString("saldo"))
                    );
                    contasusuario.setId(Integer.parseInt(resultado.getString("id")));
                    minhascontasusuario.add(contasusuario);
                }
            }
        } catch (SQLException e) {
            System.err.println("Query de select (GetAll - ContasUsuario) incorreta");
        } finally {
            conexao.closeConexao();
        }
        return minhascontasusuario;
    }
}
