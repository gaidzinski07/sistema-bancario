/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import entidade.Deposito;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 *
 * @author João
 */
public class DepositoDAO implements Dao<Deposito> {

    @Override
    public Deposito get(int id) {
        Conexao conexao = new Conexao();
        Deposito deposito = new Deposito();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM Deposito WHERE id = ?");
            sql.setInt(1, id);
            ResultSet resultado = sql.executeQuery();

            if (resultado != null) {
                while (resultado.next()) {
                    deposito = createFromResultSet(resultado);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            conexao.closeConexao();
        }
        return deposito;
    }

    @Override
    public ArrayList<Deposito> getAll() {
        Conexao conexao = new Conexao();
        ArrayList<Deposito> depositos = new ArrayList<Deposito>();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM Deposito");
            ResultSet resultado = sql.executeQuery();

            if (resultado != null) {
                while (resultado.next()) {
                    Deposito deposito = createFromResultSet(resultado);
                    depositos.add(deposito);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            conexao.closeConexao();
        }
        return depositos;
    }

    @Override
    public void insert(Deposito deposito) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("INSERT INTO Deposito (conta_bancaria, id_agencia, ts_saque, valor) VALUES (?,?,?,?)");
            sql.setInt(1, deposito.getContaBancaria());
            sql.setInt(2, deposito.getIdAgencia());
            sql.setTimestamp(3, deposito.getDataDeposito());
            sql.setFloat(4, deposito.getValor());
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            conexao.closeConexao();
        }

    }

    @Override
    public void update(Deposito deposito) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("UPDATE Deposito SET conta_bancaria = ?, id_agencia = ?, ts_saque = ?, valor = ? WHERE id = ?");
            sql.setInt(1, deposito.getContaBancaria());
            sql.setInt(2, deposito.getIdAgencia());
            sql.setTimestamp(3, deposito.getDataDeposito());
            sql.setFloat(4, deposito.getValor());
            sql.setInt(5, deposito.getId());
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
            PreparedStatement sql = conexao.getConexao().prepareStatement("DELETE FROM Deposito WHERE id = ?");
            sql.setInt(1, id);
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            conexao.closeConexao();
        }
    }

    private Deposito createFromResultSet(ResultSet resultado) {
        Deposito deposito = new Deposito();
        try {
            deposito.setId(Integer.parseInt(resultado.getString("id")));
            deposito.setContaBancaria(Integer.parseInt(resultado.getString("conta_bancaria")));
            deposito.setDataDeposito(resultado.getTimestamp("ts_deposito"));
            deposito.setIdAgencia(Integer.parseInt(resultado.getString("id_agencia")));
            deposito.setValor(Float.parseFloat(resultado.getString("valor")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deposito;

    }

}
