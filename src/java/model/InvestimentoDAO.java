/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import dto.InvestimentoDTO;
import entidade.Fundo;
import entidade.Investimento;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                    .prepareStatement("INSERT INTO investimento (conta_bancaria, id_fundo, vr_cota_inicio, vr_investido, qtd_cotas) VALUES (?,?,?,?,?)");
            sql.setInt(1, investimento.getContaBancaria());
            sql.setInt(2, investimento.getIdFundo());
            sql.setFloat(3, investimento.getVrCotaInicio());
            sql.setFloat(4, investimento.getVrInvestido());
            sql.setFloat(5, investimento.getQtdCotas());
            ContasBancariaDAO contaDao = new ContasBancariaDAO();
            sql.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
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
                    .prepareStatement("UPDATE Investimento SET conta_bancaria=?, id_fundo=?, vr_cota_incio=?, vr_investido=?, qtd_cotas=?, ts_investimento = ? WHERE id=?");
            sql.setInt(1, investimento.getContaBancaria());
            sql.setInt(2, investimento.getIdFundo());
            sql.setFloat(3, investimento.getVrCotaInicio());
            sql.setFloat(4, investimento.getVrInvestido());
            sql.setFloat(5, investimento.getQtdCotas());
            sql.setTimestamp(6, investimento.getData());
            sql.setInt(7, investimento.getId());
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
            investimento.setData(resultado.getTimestamp("ts_investimento"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return investimento;
    }

    public List<InvestimentoDTO> getInvestimentosDoCliente(int contaBancaria) {
        Conexao conexao = new Conexao();
        ArrayList<InvestimentoDTO> investimentos = new ArrayList<InvestimentoDTO>();
        try {
            String sql = "select "
                    + "i.id id, "
                    + "i.conta_bancaria conta_bancaria, "
                    + "i.id_fundo id_fundo, "
                    + "i.vr_cota_inicio vr_cota_inicio, "
                    + "i.vr_investido vr_investido, "
                    + "i.qtd_cotas qtd_cotas, "
                    + "i.ts_investimento ts_investimento, "
                    + "f.nome nome_fundo, "
                    + "f.vr_cota vr_cota_fundo "
                    + "from "
                    + "investimento i "
                    + "join fundo f "
                    + "on "
                    + "f.id = i.id_fundo "
                    + "where "
                    + "i.conta_bancaria = ? ";
            PreparedStatement ps = conexao.getConexao().prepareStatement(sql);
            ps.setInt(1, contaBancaria);
            ResultSet result = ps.executeQuery();
            if(result != null){
                while(result.next()){
                    Investimento invest = createFromResultSet(result);
                    Fundo fundo = new Fundo();
                    fundo.setNome(result.getString("nome_fundo"));
                    fundo.setValorCota(result.getFloat("vr_cota_fundo"));
                    InvestimentoDTO dto = new InvestimentoDTO(fundo, invest);
                    investimentos.add(dto);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(InvestimentoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            conexao.closeConexao();
        }
        return investimentos;
    }
    
    public void resgatar(int investimento){
        ContasBancariaDAO contaDao = new ContasBancariaDAO();
        FundoDAO fundoDao = new FundoDAO();
        Investimento entidade = get(investimento);
        Fundo fundo = fundoDao.get(entidade.getIdFundo());
        float rendimentos = fundo.getValorCota() / entidade.getVrCotaInicio();
        System.out.println(rendimentos + " * " + entidade.getVrInvestido() + " = " + entidade.getVrInvestido() * rendimentos);
        contaDao.atualizaSaldoAposOperacao(entidade.getContaBancaria(), entidade.getVrInvestido() * rendimentos);
        delete(investimento);
    }

    public boolean aplicar(int idFundo, float valor) {
        System.out.println(idFundo + " - " + valor);
        FundoDAO fundoDao = new FundoDAO();
        Fundo fundo = fundoDao.get(idFundo);
        ContasBancariaDAO contaDao = new ContasBancariaDAO();
        boolean temSaldo = contaDao.podeFazerOperacaoSaida(2, 3, valor);
        if(fundo != null && valor >= fundo.getValorMinimo() && temSaldo){
            Investimento investimento = new Investimento();
            investimento.setContaBancaria(2);
            investimento.setIdFundo(idFundo);
            investimento.setQtdCotas(valor / fundo.getValorCota());
            investimento.setVrCotaInicio(fundo.getValorCota());
            investimento.setVrInvestido(valor);
            insert(investimento);
            contaDao.atualizaSaldoAposOperacao(2, valor * -1);
        }
        return temSaldo;
    }

}
