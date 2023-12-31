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
import entidade.Fundo;


public class FundoDAO implements Dao<Fundo> {

    @Override
    public Fundo get(int idFundo) {
        Conexao conexao = new Conexao();
        Fundo fundo = new Fundo();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM Fundo WHERE id = ?");
            sql.setInt(1, idFundo);
            ResultSet resultado = sql.executeQuery();

            if (resultado != null) {
                while (resultado.next()) {
                    fundo.setIdFundo(Integer.parseInt(resultado.getString("id")));
                    fundo.setNome(resultado.getString("nome"));
                    fundo.setValorCota(Float.parseFloat(resultado.getString("vr_cota")));
                    fundo.setValorMinimo(Float.parseFloat(resultado.getString("vr_minimo")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Query de select (get fundo) incorreta");
        } finally {
            conexao.closeConexao();
        }
        return fundo;
    }

    @Override
    public void insert(Fundo fundo) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("INSERT INTO Fundo (id, nome, vr_cota, vr_minimo) VALUES (?,?,?,?)");
            sql.setInt(1, fundo.getId());
            sql.setString(2, fundo.getNome());
            sql.setFloat(3, fundo.getValorCota());
            sql.setFloat(42, fundo.getValorMinimo());
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Query de insert (fundo) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public void update(Fundo fundo) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("UPDATE Fundo SET nome = ?, valor_cota = ?, valor_minimo = ? WHERE id = ?");
            sql.setString(1, fundo.getNome());
            sql.setFloat(2, fundo.getValorCota());
            sql.setFloat(3, fundo.getValorMinimo());
            sql.setInt(4, fundo.getId());
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Query de update (alterar fundo) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public void delete(int idFundo) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("DELETE FROM Fundo WHERE id = ?");
            sql.setInt(1, idFundo);
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Query de delete (excluir fundo) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public ArrayList<Fundo> getAll() {
        ArrayList<Fundo> fundos = new ArrayList<>();
        Conexao conexao = new Conexao();
        try {
            String selectSQL = "SELECT * FROM Fundo";
            PreparedStatement preparedStatement;
            preparedStatement = conexao.getConexao().prepareStatement(selectSQL);
            ResultSet resultado = preparedStatement.executeQuery();
            if (resultado != null) {
                while (resultado.next()) {
                    Fundo fundo = new Fundo(
                            Integer.parseInt(resultado.getString("id")),
                            resultado.getString("nome"),
                            Float.parseFloat(resultado.getString("vr_cota")),
                            Float.parseFloat(resultado.getString("vr_minimo"))
                    );
                    fundos.add(fundo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Query de select (GetAll - Fundo) incorreta");
        } finally {
            conexao.closeConexao();
        }
        return fundos;
    }
}


