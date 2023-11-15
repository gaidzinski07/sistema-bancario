package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import entidade.Contas;

/*
--
-- Estrutura da tabela `comentarios`
--

CREATE TABLE IF NOT EXISTS `comentarios` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `comentario` varchar(255) NOT NULL,
  `data` date DEFAULT NULL,
  `idusuario` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_usuario` (`idusuario`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

 */
public class ContasDAO implements Dao<Contas> {

    @Override
    public Contas get(int tipo) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM Comentarios WHERE TIPO = ? ");
            sql.setInt(1, tipo);
            ResultSet resultado = sql.executeQuery();
            Contas conta = new Contas();

            if (resultado != null) {
                while (resultado.next()) {
                    conta.setTipo(Integer.parseInt(resultado.getString("tipo")));
                    conta.setDescricao(resultado.getString("descricao"));                    
                }
            }
            return conta;

        } catch (SQLException e) {
            throw new RuntimeException("Query de select (get conta) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public void insert(Contas t) {

        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("INSERT INTO Contas (tipo, descricao) VALUES (?,?)");
            sql.setInt(1, t.getTipo());
            sql.setString(2, t.getDescricao());
            sql.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Query de insert (comentario) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public void update(Contas t) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("UPDATE Contas SET DESCRICAO = ?  WHERE TIPO = ? ");
            sql.setInt(1, t.getTipo());
            sql.setString(2, t.getDescricao());
            sql.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Query de update (alterar comentario) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public void delete(int tipo) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("DELETE FROM Contas WHERE TIPO = ? ");
            sql.setInt(1, tipo);
            sql.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Query de delete (excluir comentario) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public ArrayList<Contas> getAll() {

        ArrayList<Contas> todasContas = new ArrayList();
        Conexao conexao = new Conexao();
        try {
            String selectSQL = "SELECT * FROM Contas";
            PreparedStatement preparedStatement;
            preparedStatement = conexao.getConexao().prepareStatement(selectSQL);
            ResultSet resultado = preparedStatement.executeQuery();
            if (resultado != null) {
                while (resultado.next()) {
                    Contas Conta = new Contas(resultado.getInt("TIPO"),resultado.getString("DESCRICAO"));                       
                    
                    todasContas.add(Conta);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query de select (GetAll) incorreta");
        } finally {
            conexao.closeConexao();
        }
        return todasContas;
    }
}
