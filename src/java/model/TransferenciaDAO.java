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
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM Transferencia WHERE id_transferencia = ?");
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
                    .prepareStatement("INSERT INTO Transferencia (id_usuario_origem, id_usuario_destino, data, valor) VALUES (?,?,?,?)");
            sql.setInt(1, transferencia.getIdUsuarioOrigem());
            sql.setInt(2, transferencia.getIdUsuarioDestino());
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
                    .prepareStatement("UPDATE Transferencia SET id_usuario_origem=?, id_usuario_destino=?, data=?, valor=? WHERE id_transferencia=?");
            sql.setInt(1, transferencia.getIdUsuarioOrigem());
            sql.setInt(2, transferencia.getIdUsuarioDestino());
            sql.setTimestamp(3, new Timestamp(transferencia.getData().getTime()));
            sql.setFloat(4, transferencia.getValor());
            sql.setInt(5, transferencia.getIdTransferencia());
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
                    .prepareStatement("DELETE FROM Transferencia WHERE id_transferencia=?");
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
            transferencia.setIdTransferencia(Integer.parseInt(resultado.getString("id_transferencia")));
            transferencia.setIdUsuarioOrigem(Integer.parseInt(resultado.getString("id_usuario_origem")));
            transferencia.setIdUsuarioDestino(Integer.parseInt(resultado.getString("id_usuario_destino")));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            transferencia.setData(formatter.parse(resultado.getString("data")));
            transferencia.setValor(Float.parseFloat(resultado.getString("valor")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transferencia;
    }

}
