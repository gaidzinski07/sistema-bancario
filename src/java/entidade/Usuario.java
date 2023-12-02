package entidade;

import java.util.Date;

public class Usuario {

    private int id;
    private String nome;
    private String cpf;
    private String endereco;
    private Date dataNascimento;
    private String senha;

    //public Usuario() {
    //}

    public Usuario(int id, String nome, String cpf, String endereco, Date dataNascimento, String senha) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.endereco = endereco;
        this.dataNascimento = dataNascimento;
        this.senha = senha;
    }
    
    public Usuario() {
        this.id = 0;
        this.nome = "";
        this.cpf = "";
        this.endereco = "";
        this.dataNascimento = null;
        this.senha = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public Date getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(Date dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
    
}
