/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import entidade.Transferencia;
import java.util.ArrayList;
import entidade.Investimento;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 *
 * @author João
 */
public class TransferenciaDAO implements Dao<Transferencia> {

    @Override
    public Transferencia get(int id) {
        Conexao conexao = new Conexao();
        Transferencia transferencia = new Transferencia();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM Transferencia WHERE id = ?");
            sql.setInt(1, id);
            ResultSet resultado = sql.executeQuery();

            if (resultado != null) {
                while (resultado.next()) {
                    transferencia = createFromResultSet(resultado);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            conexao.closeConexao();
        }
        return transferencia;
    }

    @Override
    public ArrayList<Transferencia> getAll() {
        Conexao conexao = new Conexao();
        ArrayList<Transferencia> transferencias = new ArrayList<Transferencia>();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM Transferencia");
            ResultSet resultado = sql.executeQuery();

            if (resultado != null) {
                while (resultado.next()) {
                    Transferencia transferencia = createFromResultSet(resultado);
                    transferencias.add(transferencia);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            conexao.closeConexao();
        }
        return transferencias;
    }

    @Override
    public void insert(Transferencia transferencia) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao()
                    .prepareStatement("INSERT INTO Transferencia (conta_bancaria_origem, conta_bancaria_destino, ts_deposito, valor) VALUES (?,?,?,?)");
            sql.setInt(1, transferencia.getContaBancariaOrigem());
            sql.setInt(2, transferencia.getContaBancariaDestino());
            sql.setTimestamp(3, new Timestamp(transferencia.getData().getTime()));
            sql.setFloat(4, transferencia.getValor());
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public void update(Transferencia transferencia) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao()
                    .prepareStatement("UPDATE Transferencia SET conta_bancaria_origem=?, conta_bancaria_destino=?, ts_deposito=?, valor=? WHERE id=?");
            sql.setInt(1, transferencia.getContaBancariaOrigem());
            sql.setInt(2, transferencia.getContaBancariaDestino());
            sql.setTimestamp(3, new Timestamp(transferencia.getData().getTime()));
            sql.setFloat(4, transferencia.getValor());
            sql.setInt(5, transferencia.getId());
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
                    .prepareStatement("DELETE FROM Transferencia WHERE id=?");
            sql.setInt(1, id);
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            conexao.closeConexao();
        }
    }

    private Transferencia createFromResultSet(ResultSet resultado) {
        Transferencia transferencia = new Transferencia();
        try {
            transferencia.setId(Integer.parseInt(resultado.getString("id")));
            transferencia.setContaBancariaOrigem(Integer.parseInt(resultado.getString("conta_bancaria_origem")));
            transferencia.setContaBancariaDestino(Integer.parseInt(resultado.getString("conta_bancaria_destino")));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            transferencia.setData(formatter.parse(resultado.getString("ts_deposito")));
            transferencia.setValor(Float.parseFloat(resultado.getString("valor")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transferencia;
    }

}
