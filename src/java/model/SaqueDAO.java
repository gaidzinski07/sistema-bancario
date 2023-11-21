/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import entidade.Saque;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 *
 * @author João
 */
public class SaqueDAO implements Dao<Saque> {

    @Override
    public Saque get(int id) {
        Conexao conexao = new Conexao();
        Saque saque = new Saque();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM Saque WHERE id_saque = ?");
            sql.setInt(1, id);
            ResultSet resultado = sql.executeQuery();

            if (resultado != null) {
                while (resultado.next()) {
                    saque = createFromResultSet(resultado);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            conexao.closeConexao();
        }
        return saque;
    }

    @Override
    public ArrayList<Saque> getAll() {
        Conexao conexao = new Conexao();
        ArrayList<Saque> saques = new ArrayList<Saque>();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM Saque");
            ResultSet resultado = sql.executeQuery();

            if (resultado != null) {
                while (resultado.next()) {
                    Saque saque = createFromResultSet(resultado);
                    saques.add(saque);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            conexao.closeConexao();
        }
        return saques;
    }

    @Override
    public void insert(Saque saque) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao()
                    .prepareStatement("INSERT INTO Saque (id_usuario, id_agencia, data, local, valor) VALUES (?,?,?,?,?)");
            sql.setInt(1, saque.getIdUsuario());
            sql.setInt(2, saque.getIdAgencia());
            sql.setTimestamp(3, new Timestamp(saque.getData().getTime()));
            sql.setString(4, saque.getLocal());
            sql.setFloat(5, saque.getValor());
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public void update(Saque saque) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao()
                    .prepareStatement("UPDATE Saque SET id_usuario=?, id_agencia=?, data=?, local=?, valor=?");
            sql.setInt(1, saque.getIdUsuario());
            sql.setInt(2, saque.getIdAgencia());
            sql.setTimestamp(3, new Timestamp(saque.getData().getTime()));
            sql.setString(4, saque.getLocal());
            sql.setFloat(5, saque.getValor());
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
            PreparedStatement sql = conexao.getConexao().prepareStatement("DELETE FROM Saque WHERE id_saque = ?");
            sql.setInt(1, id);
            sql.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            conexao.closeConexao();
        }
    }

    private Saque createFromResultSet(ResultSet resultado) {
        Saque saque = new Saque();
        try {
            saque.setIdSaque(Integer.parseInt(resultado.getString("id_saque")));
            saque.setIdUsuario(Integer.parseInt(resultado.getString("id_usuario")));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            saque.setData(formatter.parse(resultado.getString("data")));
            saque.setLocal(resultado.getString("local"));
            saque.setIdAgencia(Integer.parseInt(resultado.getString("id_agencia")));
            saque.setValor(Float.parseFloat(resultado.getString("valor")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return saque;
    }

}
