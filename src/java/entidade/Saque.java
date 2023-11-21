/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entidade;

import java.util.Date;

/**
 *
 * @author João
 */
public class Saque {
    
    private int idSaque;
    private int idUsuario;
    private int idAgencia;
    private Date data;
    private String local;
    private float valor;

    public Saque() {
    }

    public Saque(int id_saque, int id_usuario, int id_agencia, Date data, String local, float valor) {
        this.idSaque = id_saque;
        this.idUsuario = id_usuario;
        this.idAgencia = id_agencia;
        this.data = data;
        this.local = local;
        this.valor = valor;
    }

    public int getIdSaque() {
        return idSaque;
    }

    public void setIdSaque(int idSaque) {
        this.idSaque = idSaque;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdAgencia() {
        return idAgencia;
    }

    public void setIdAgencia(int idAgencia) {
        this.idAgencia = idAgencia;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public float getValor() {
        return valor;
    }

    public void setValor(float valor) {
        this.valor = valor;
    }
    
    
}
