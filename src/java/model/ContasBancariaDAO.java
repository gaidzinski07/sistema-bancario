/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import dto.TransacaoDTO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import entidade.ContaBancaria;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                    contaBancaria.setIdAgencia(Integer.parseInt(resultado.getString("id_agencia")));
                    contaBancaria.setSaldoAtual(Float.parseFloat(resultado.getString("saldo")));
                    contaBancaria.setSaldoAtualPP(Float.parseFloat(resultado.getString("saldoPP")));
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
            PreparedStatement sql = conexao.getConexao().prepareStatement("UPDATE CONTA_BANCARIA SET id_agencia = ?, saldo = ?, saldoPP = ? WHERE conta_corrente = ?");
            sql.setInt(1, contaBancaria.getIdAgencia());
            sql.setFloat(2, contaBancaria.getSaldoAtual());            
            sql.setFloat(3, contaBancaria.getSaldoAtualPP());
            sql.setInt(4, contaBancaria.getContaCorrente());
            
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
                            Float.parseFloat(resultado.getString("saldo")),
                            Float.parseFloat(resultado.getString("saldoPP")));
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

    public boolean podeFazerOperacaoSaida(int conta, int agencia, float valor) {
        Conexao conexao = new Conexao();
        try {
            String sql = "select (cb.saldo >= ?) tem_saldo "
                    + "from conta_bancaria cb "
                    + "where cb.conta_corrente  = ? and id_agencia = ? ";
            PreparedStatement preparedStatement;
            preparedStatement = conexao.getConexao().prepareStatement(sql);
            preparedStatement.setFloat(1, valor);
            preparedStatement.setFloat(2, conta);
            preparedStatement.setFloat(3, agencia);
            ResultSet resultado = preparedStatement.executeQuery();
            if (resultado != null) {
                while (resultado.next()) {
                    return resultado.getInt("tem_saldo") == 1;
                }
            }
            return false;
        } catch (SQLException ex) {
            Logger.getLogger(ContasBancariaDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            conexao.closeConexao();
        }
        return false;
    }

    public void atualizaSaldoAposOperacao(int conta, float valorOperacao) {
        ContaBancaria contaBancaria = get(conta);
        contaBancaria.setSaldoAtual(contaBancaria.getSaldoAtual() + valorOperacao);
        update(contaBancaria);
    }

    public List<TransacaoDTO> getExtrato(ContaBancaria conta) {
        Conexao conexao = new Conexao();

        try {
            String sql = "(select 'TRANSFERENCIA ENVIADA' tipo, t.id id, t.ts_transferencia dt, t.valor vr from transferencia t where t.conta_bancaria_origem = ? "
                    + "union all "
                    + "select 'SAQUE' tipo, s.id id, s.ts_saque dt, s.valor vr from saque s where s.conta_bancaria = ? "
                    + "union all "
                    + "select 'INVESTIMENTO' tipo, i.id id, i.ts_investimento dt, i.vr_investido vr from investimento i where i.conta_bancaria = ? "
                    + "union all "
                    + "select 'TRANSFERENCIA RECEBIDA' tipo, t2.id id, t2.ts_transferencia dt, t2.valor vr from transferencia t2 where t2.conta_bancaria_destino = ? "
                    + "union all "
                    + "select 'DEPOSITO' tipo, d.id id, d.ts_deposito dt, d.valor vr from deposito d where d.conta_bancaria = ?) "
                    + "order by dt asc; ";
            PreparedStatement preparedStatement = conexao.getConexao().prepareStatement(sql);
            preparedStatement.setInt(1, conta.getContaCorrente());
            preparedStatement.setInt(2, conta.getContaCorrente());
            preparedStatement.setInt(3, conta.getContaCorrente());
            preparedStatement.setInt(4, conta.getContaCorrente());
            preparedStatement.setInt(5, conta.getContaCorrente());
            ResultSet result = preparedStatement.executeQuery();
            List<TransacaoDTO> transacoes = new ArrayList<TransacaoDTO>();
            if(result != null){
                while(result.next()){
                    TransacaoDTO dto = new TransacaoDTO();
                    dto.setTipo(result.getString("tipo"));
                    dto.setData(result.getTimestamp("dt"));
                    dto.setId(result.getInt("id"));
                    dto.setValor(result.getFloat("vr"));
                    transacoes.add(dto);
                }
            }
            return transacoes;
        } catch (SQLException ex) {
            Logger.getLogger(ContasBancariaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            conexao.closeConexao();
        }
        return new ArrayList<TransacaoDTO>();
    }
}
