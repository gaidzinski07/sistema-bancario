/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import entidade.Investimento;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author João
 */
public class InvestimentoDAO implements Dao<Investimento> {

    @Override
    public Investimento get(int id) {
        Conexao conexao = new Conexao();
        Investimento investimento = new Investimento();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM Investimento WHERE id = ?");
            sql.setInt(1, id);
            ResultSet resultado = sql.executeQuery();

            if (resultado != null) {
                while (resultado.next()) {
                    investimento = createFromResultSet(resultado);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            conexao.closeConexao();
        }
        return investimento;
    }

    @Override
    public ArrayList<Investimento> getAll() {
        Conexao conexao = new Conexao();
        ArrayList<Investimento> investimentos = new ArrayList<Investimento>();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM Investimento");
            ResultSet resultado = sql.executeQuery();

            if (resultado != null) {
                while (resultado.next()) {
                    Investimento investimento = createFromResultSet(resultado);
                    investimentos.add(investimento);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            conexao.closeConexao();
        }
        return investimentos;
    }

    @Override
    public void insert(Investimento investimento) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao()
                    .prepareStatement("INSERT INTO Investimento (conta_bancaria, id_fundo, vr_cota_incio, vr_investido, qtd_cotas) VALUES (?,?,?,?,?)");
            sql.setInt(1, investimento.getContaBancaria());
            sql.setInt(2, investimento.getIdFundo());
            sql.setFloat(3, investimento.getVrCotaInicio());
            sql.setFloat(4, investimento.getVrInvestido());
            sql.setFloat(5, investimento.getQtdCotas());
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public void update(Investimento investimento) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao()
                    .prepareStatement("UPDATE Investimento SET conta_bancaria=?, id_fundo=?, vr_cota_incio=?, vr_investido=?, qtd_cotas=? WHERE id=?");
            sql.setInt(1, investimento.getContaBancaria());
            sql.setInt(2, investimento.getIdFundo());
            sql.setFloat(3, investimento.getVrCotaInicio());
            sql.setFloat(4, investimento.getVrInvestido());
            sql.setFloat(5, investimento.getQtdCotas());
            sql.setInt(5, investimento.getId());
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public void delete(int id) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao()
                    .prepareStatement("DELETE FROM Investimento WHERE id = ?");
            sql.setInt(1, id);
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            conexao.closeConexao();
        }
    }

    private Investimento createFromResultSet(ResultSet resultado) {
        Investimento investimento = new Investimento();
        try {
            investimento.setId(Integer.parseInt(resultado.getString("id")));
            investimento.setContaBancaria(Integer.parseInt(resultado.getString("conta_bancaria")));
            investimento.setIdFundo(Integer.parseInt(resultado.getString("id_fundo")));
            investimento.setVrInvestido(Float.parseFloat(resultado.getString("vr_investido")));
            investimento.setVrCotaInicio(Float.parseFloat(resultado.getString("vr_cota_inicio")));
            investimento.setQtdCotas(Float.parseFloat(resultado.getString("qtd_cotas")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return investimento;
    }

}
