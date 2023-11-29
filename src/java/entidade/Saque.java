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
    
    private int id;
    private int contaBancaria;
    private int idAgencia;
    private Date data;
    private float valor;

    public Saque() {
    }

    public Saque(int id, int contaBancaria, int idAgencia, Date data, float valor) {
        this.id = id;
        this.contaBancaria = contaBancaria;
        this.idAgencia = idAgencia;
        this.data = data;
        this.valor = valor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getContaBancaria() {
        return contaBancaria;
    }

    public void setContaBancaria(int contaBancaria) {
        this.contaBancaria = contaBancaria;
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

    public float getValor() {
        return valor;
    }

    public void setValor(float valor) {
        this.valor = valor;
    }
    
}
