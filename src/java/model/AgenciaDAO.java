/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author lucas
 */

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import entidade.Agencia;

public class AgenciaDAO implements Dao<Agencia> {

    @Override
    public Agencia get(int id) {
        Conexao conexao = new Conexao();
        Agencia agencia = new Agencia();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM Agencia WHERE id = ?");
            sql.setInt(1, id);
            ResultSet resultado = sql.executeQuery();

            if (resultado != null) {
                while (resultado.next()) {
                    agencia.setId(Integer.parseInt(resultado.getString("id")));
                    agencia.setEndereco(resultado.getString("endereco"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Query de select (get agencia) incorreta");
        } finally {
            conexao.closeConexao();
        }
        return agencia;
    }

    @Override
    public void insert(Agencia agencia) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("INSERT INTO Agencia (endereco) VALUES (?)");
            sql.setString(1, agencia.getEndereco());
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Query de insert (agencia) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public void update(Agencia agencia) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("UPDATE Agencia SET endereco = ? WHERE id = ?");
            sql.setString(1, agencia.getEndereco());
            sql.setInt(2, agencia.getId());
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Query de update (alterar agencia) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public void delete(int id) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("DELETE FROM Agencia WHERE id = ?");
            sql.setInt(1, id);
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Query de delete (excluir agencia) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public ArrayList<Agencia> getAll() {
        ArrayList<Agencia> agencias = new ArrayList<>();
        Conexao conexao = new Conexao();
        try {
            String selectSQL = "SELECT * FROM Agencia";
            PreparedStatement preparedStatement;
            preparedStatement = conexao.getConexao().prepareStatement(selectSQL);
            ResultSet resultado = preparedStatement.executeQuery();
            if (resultado != null) {
                while (resultado.next()) {
                    Agencia agencia = new Agencia(
                            Integer.parseInt(resultado.getString("id")),
                            resultado.getString("endereco")
                    );
                    agencias.add(agencia);
                }
            }
        } catch (SQLException e) {
            System.err.println("Query de select (GetAll - Agencia) incorreta");
        } finally {
            conexao.closeConexao();
        }
        return agencias;
    }
}
