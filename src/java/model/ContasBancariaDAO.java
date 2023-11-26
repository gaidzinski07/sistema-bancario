/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import entidade.ContaBancaria;

public class ContasBancariaDAO implements Dao<ContaBancaria> {

    @Override
    public ContaBancaria get(int contaCorrente) {
        Conexao conexao = new Conexao();
        ContaBancaria contaBancaria = new ContaBancaria();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM CONTA_BANCARIA WHERE conta_corrente = ?");
            sql.setInt(1, contaCorrente);
            ResultSet resultado = sql.executeQuery();

            if (resultado != null) {
                while (resultado.next()) {
                    contaBancaria.setContaCorrente(Integer.parseInt(resultado.getString("conta_corrente")));
                    contaBancaria.setSaldoAtual(Float.parseFloat(resultado.getString("saldo")));
                    contaBancaria.setIdAgencia(Integer.parseInt(resultado.getString("id_agencia")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Query de select (get conta bancaria) incorreta");
        } finally {
            conexao.closeConexao();
        }
        return contaBancaria;
    }

    @Override
    public void insert(ContaBancaria contaBancaria) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("INSERT INTO CONTA_BANCARIA (conta_corrente, saldo, id_agencia) VALUES (?,?,?)");
            sql.setInt(1, contaBancaria.getContaCorrente());
            sql.setFloat(2, contaBancaria.getSaldoAtual());
            sql.setInt(3, contaBancaria.getIdAgencia());
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Query de insert (conta bancaria) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public void update(ContaBancaria contaBancaria) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("UPDATE CONTA_BANCARIA saldo = ?, id_agencia = ? WHERE conta_corrente = ?");
            sql.setFloat(3, contaBancaria.getSaldoAtual());
            sql.setInt(4, contaBancaria.getIdAgencia());
            sql.setInt(5, contaBancaria.getContaCorrente());
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Query de update (alterar conta bancaria) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public void delete(int contaCorrente) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("DELETE FROM CONTA_BANCARIA WHERE conta_corrente = ?");
            sql.setInt(1, contaCorrente);
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Query de delete (excluir conta bancaria) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public ArrayList<ContaBancaria> getAll() {
        ArrayList<ContaBancaria> contasBancarias = new ArrayList<>();
        Conexao conexao = new Conexao();
        try {
            String selectSQL = "SELECT * FROM CONTA_BANCARIA";
            PreparedStatement preparedStatement;
            preparedStatement = conexao.getConexao().prepareStatement(selectSQL);
            ResultSet resultado = preparedStatement.executeQuery();
            if (resultado != null) {
                while (resultado.next()) {
                    ContaBancaria contaBancaria = new ContaBancaria(
                            Integer.parseInt(resultado.getString("conta_corrente")),
                            Integer.parseInt(resultado.getString("id_agencia")),
                            Float.parseFloat(resultado.getString("saldo_atual")));
                    contasBancarias.add(contaBancaria);
                }
            }
        } catch (SQLException e) {
            System.err.println("Query de select (GetAll - Conta Bancaria) incorreta");
        } finally {
            conexao.closeConexao();
        }
        return contasBancarias;
    }
}
