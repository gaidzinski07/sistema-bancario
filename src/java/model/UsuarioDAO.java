package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import entidade.Usuario;
import entidade.Cliente;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsuarioDAO {

    public void inserir(Usuario usuario) throws Exception {
        System.out.println("Entrei na classe UsuarioDAO");
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("INSERT INTO USUARIO (nome, cpf, endereco, data_nascimento, senha)"
                    + " VALUES (?,?,?,?,?)");
            sql.setString(1, usuario.getNome());
            sql.setString(2, usuario.getCpf());
            sql.setString(3, usuario.getEndereco());
            sql.setTimestamp(4, new Timestamp(usuario.getDataNascimento().getTime()));
            sql.setString(5, usuario.getSenha());
            sql.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException();
        } finally {
            conexao.closeConexao();
        }
    }

    public Usuario getUsuario(int id) throws Exception {
        Conexao conexao = new Conexao();
        try {
            Usuario usuario = new Usuario();
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM USUARIO WHERE ID = ?");
            sql.setInt(1, id);
            ResultSet resultado = sql.executeQuery();
            if (resultado != null) {
                while (resultado.next()) {
                    usuario.setId(Integer.parseInt(resultado.getString("id")));
                    usuario.setNome(resultado.getString("NOME"));
                    usuario.setCpf(resultado.getString("CPF"));
                    usuario.setEndereco(resultado.getString("endereco"));
                    usuario.setSenha(resultado.getString("SENHA"));
                }
            }
            return usuario;

        } catch (SQLException e) {
            throw new RuntimeException("Query de select (get) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    public void alterar(Usuario usuario) throws Exception {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("UPDATE USUARIO SET nome = ?, cpf = ?, endereco = ?, data_nascimento = ?, senha = ?  WHERE ID = ? ");
            sql.setString(1, usuario.getNome());
            sql.setString(2, usuario.getCpf());
            sql.setString(3, usuario.getEndereco());
            sql.setTimestamp(4, new Timestamp(usuario.getDataNascimento().getTime()));
            sql.setString(5, usuario.getSenha());
            sql.setInt(6, usuario.getId());
            sql.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Query de update (alterar) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    public void excluir(Usuario usuario) throws Exception {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("DELETE FROM USUARIO WHERE ID = ? ");
            sql.setInt(1, usuario.getId());
            sql.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Query de delete (excluir) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    public ArrayList<Usuario> listaDeUsuarios() {
        ArrayList<Usuario> meusUsuarios = new ArrayList();
        Conexao conexao = new Conexao();
        try {
            String selectSQL = "SELECT * FROM USUARIO order by nome";
            PreparedStatement preparedStatement;
            preparedStatement = conexao.getConexao().prepareStatement(selectSQL);
            ResultSet resultado = preparedStatement.executeQuery();
            if (resultado != null) {
                while (resultado.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setId(Integer.parseInt(resultado.getString("ID")));
                    usuario.setNome(resultado.getString("NOME"));
                    usuario.setCpf(resultado.getString("CPF"));
                    usuario.setEndereco(resultado.getString("endereco"));
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    usuario.setDataNascimento(formatter.parse(resultado.getString("ts_deposito")));
                    usuario.setSenha(resultado.getString("SENHA"));
                    meusUsuarios.add(usuario);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query de select (ListaDeUsuarios) incorreta");
        } catch (ParseException ex) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            conexao.closeConexao();
        }
        return meusUsuarios;
    }

    public Usuario logar(Usuario usuario) throws Exception {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM USUARIO WHERE cpf=? and senha =? LIMIT 1");
            sql.setString(1, usuario.getCpf());
            sql.setString(2, usuario.getSenha());
            ResultSet resultado = sql.executeQuery();
            Usuario usuarioObtido = new Usuario();
            if (resultado != null) {
                while (resultado.next()) {
                    usuarioObtido.setId(Integer.parseInt(resultado.getString("ID")));
                    usuarioObtido.setNome(resultado.getString("NOME"));
                    usuarioObtido.setCpf(resultado.getString("CPF"));
                    usuarioObtido.setEndereco(resultado.getString("endereco"));

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = formatter.parse(resultado.getString("data_nascimento"));
                    usuarioObtido.setDataNascimento(date);
                    usuarioObtido.setSenha(resultado.getString("SENHA"));
                }
            }
            return usuarioObtido;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Query de select (get) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }
    
        public Usuario logarCliente(Usuario usuario) throws Exception {
        Conexao conexao = new Conexao();
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM USUARIO WHERE Id=? and senha =? LIMIT 1");
            sql.setInt(1, usuario.getId());
            sql.setString(2, usuario.getSenha());
            System.out.println("sql " + sql);
            ResultSet resultado = sql.executeQuery();
            Usuario usuarioObtido = new Usuario();
            if (resultado != null) {
                while (resultado.next()) {
                    usuarioObtido.setId(Integer.parseInt(resultado.getString("ID")));
                    usuarioObtido.setNome(resultado.getString("NOME"));
                    usuarioObtido.setCpf(resultado.getString("CPF"));
                    usuarioObtido.setEndereco(resultado.getString("endereco"));

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = formatter.parse(resultado.getString("data_nascimento"));
                    usuarioObtido.setDataNascimento(date);
                    usuarioObtido.setSenha(resultado.getString("SENHA"));
                }
            }
            return usuarioObtido;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Query de select (get) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    public Usuario logarContaCorrente(Integer ContaCorrente) throws Exception {
        Conexao conexao = new Conexao();
        int idusuario;
        try {
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * contasbancaria c JOIN cliente cli on c.id_cliente = cli.id where c.conta_corrente = ? ");
            sql.setInt(1, ContaCorrente);
            ResultSet resultado1 = sql.executeQuery();
            Cliente clienteObtido = new Cliente();
            if (resultado1 != null) {
                while (resultado1.next()) {
                    clienteObtido.setIdUsuario(Integer.parseInt(resultado1.getString("id_usuario")));
                    clienteObtido.setTipoCliente(resultado1.getString("tipo_cliente"));
                    clienteObtido.setContaBancaria(Integer.parseInt(resultado1.getString("conta_bancaria")));
                }
            }
            idusuario = clienteObtido.getIdUsuario();

            PreparedStatement sql2 = conexao.getConexao().prepareStatement("SELECT * FROM USUARIO WHERE id=? LIMIT 1");
            sql2.setInt(1, idusuario);
            ResultSet resultado2 = sql2.executeQuery();
            Usuario usuarioObtido = new Usuario();
            if (resultado2 != null) {
                while (resultado2.next()) {
                    usuarioObtido.setId(Integer.parseInt(resultado2.getString("ID")));
                    usuarioObtido.setNome(resultado2.getString("NOME"));
                    usuarioObtido.setCpf(resultado2.getString("CPF"));
                    usuarioObtido.setEndereco(resultado2.getString("endereco"));
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    usuarioObtido.setDataNascimento(formatter.parse(resultado2.getString("ts_deposito")));
                    usuarioObtido.setSenha(resultado2.getString("SENHA"));
                }
            }
            return usuarioObtido;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Query de select (get) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

    public Usuario getUsuarioCPF(String cpf) throws Exception {
        Conexao conexao = new Conexao();
        try {
            Usuario usuario = new Usuario();
            PreparedStatement sql = conexao.getConexao().prepareStatement("SELECT * FROM USUARIO WHERE CPF = ? ");
            sql.setString(1, cpf);
            ResultSet resultado = sql.executeQuery();
            if (resultado != null) {
                while (resultado.next()) {
                    usuario.setId(Integer.parseInt(resultado.getString("ID")));
                    usuario.setNome(resultado.getString("NOME"));
                    usuario.setCpf(resultado.getString("CPF"));
                    usuario.setEndereco(resultado.getString("endereco"));
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    usuario.setDataNascimento(formatter.parse(resultado.getString("ts_deposito")));
                    usuario.setSenha(resultado.getString("SENHA"));
                }
            }
            return usuario;

        } catch (SQLException e) {
            throw new RuntimeException("Query de select (get) incorreta");
        } finally {
            conexao.closeConexao();
        }
    }

}
