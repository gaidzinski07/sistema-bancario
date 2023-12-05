package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import entidade.Cliente;

public class ClienteDAO implements Dao<Cliente> {

    @Override
    public Cliente get(int id_user) {
        Conexao conexao = new Conexao();
        Cliente cliente = new Cliente();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM Cliente WHERE ID_USUARIO = ? ");
            sql.setInt(1, id_user);
            ResultSet resultado = sql.executeQuery();

            if (resultado != null) {
                while (resultado.next()) {
                    cliente.setIdUsuario(Integer.parseInt(resultado.getString("id_usuario")));
                    cliente.setTipoCliente(String.valueOf(resultado.getString("tipo_cliente")));
                    cliente.setContaBancaria(Integer.parseInt(resultado.getString("conta_bancaria")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Query de select (get categoria) incorreta");
        } finally {
            conexao.closeConexao();
        }
        return cliente;
    }
    
    
    public Cliente getClienteFromContaBancaria(int contaBancaria) {
        Conexao conexao = new Conexao();
        Cliente cliente = new Cliente();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM Cliente WHERE conta_bancaria = ? ");
            sql.setInt(1, contaBancaria);
            ResultSet resultado = sql.executeQuery();

            if (resultado != null) {
                while (resultado.next()) {
                    cliente.setIdUsuario(Integer.parseInt(resultado.getString("id_usuario")));
                    cliente.setTipoCliente(String.valueOf(resultado.getString("tipo_cliente")));
                    cliente.setContaBancaria(Integer.parseInt(resultado.getString("conta_bancaria")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Query de select (get categoria) incorreta");
        } finally {
            conexao.closeConexao();
        }
        return cliente;
    }


    @Override
    public void insert(Cliente t) {

        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("INSERT INTO Cliente (tipo_cliente, conta_bancaria) VALUES (?, ?)");
            sql.setString(1, String.valueOf(t.getTipoCliente()));
            sql.setString(2, String.valueOf(t.getContaBancaria()));
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Query de insert (categoria) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public void update(Cliente t) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("UPDATE cliente SET tipo_cliente = ?, conta_bancaria = ?  WHERE ID_USUARIO = ? ");
            sql.setString(1, t.getTipoCliente());
            sql.setInt(2, t.getContaBancaria());
            sql.setInt(3, t.getIdUsuario());
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Query de update (alterar categoria) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public void delete(int id_user) {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("DELETE FROM Cliente WHERE ID_USUARIO = ? ");
            sql.setInt(1, id_user);
            sql.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Query de delete (excluir contasusuario) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    @Override
    public ArrayList<Cliente> getAll() {
        ArrayList<Cliente> clientes = new ArrayList<>();
        Conexao conexao = new Conexao();
        try {
            String selectSQL = "SELECT * FROM Cliente";
            PreparedStatement preparedStatement = conexao.getConexao().prepareStatement(selectSQL);
            ResultSet resultado = preparedStatement.executeQuery();

            while (resultado.next()) {
                Cliente cliente = new Cliente();
                cliente.setIdUsuario(Integer.parseInt(resultado.getString("id_usuario")));
                cliente.setTipoCliente(resultado.getString("tipo_cliente"));
                cliente.setContaBancaria(Integer.parseInt(resultado.getString("conta_bancaria")));
                clientes.add(cliente);
            }
        } catch (SQLException e) {
            System.err.println("Query de select (GetAll - ContasUsuario) incorreta");
        } finally {
            conexao.closeConexao();
        }
        return clientes;
    }

}
