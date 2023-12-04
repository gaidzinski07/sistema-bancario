/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entidade;

import java.util.Date;
import java.sql.Timestamp;

/**
 *
 * @author João
 */
public class Deposito {
    
    private int id;
    private int contaBancaria;
    private Timestamp dataDeposito;
    private int idAgencia;
    private float valor;
    private int tipo;

    public Deposito() {
    }

    public Deposito(int id, int contaBancaria, Timestamp dataDeposito, int idAgencia, float valor, int tipo) {
        this.id = id;
        this.contaBancaria = contaBancaria;
        this.dataDeposito = dataDeposito;
        this.idAgencia = idAgencia;
        this.valor = valor;
        this.tipo = tipo;
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

    public Timestamp getDataDeposito() {
        return dataDeposito;
    }

    public void setDataDeposito(Timestamp dataDeposito) {
        this.dataDeposito = dataDeposito;
    }

    public int getIdAgencia() {
        return idAgencia;
    }

    public void setIdAgencia(int idAgencia) {
        this.idAgencia = idAgencia;
    }

    public float getValor() {
        return valor;
    }

    public void setValor(float valor) {
        this.valor = valor;
    }
    
    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }
   
}
