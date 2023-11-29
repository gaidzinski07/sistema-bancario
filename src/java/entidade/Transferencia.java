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
public class Transferencia {
    private int id;
    private int contaBancariaOrigem;
    private int contaBancariaDestino;
    private Timestamp data;
    private float valor;

    public Transferencia() {
    }

    public Transferencia(int id, int contaBancariaOrigem, int contaBancariaDestino, Timestamp data, float valor) {
        this.id = id;
        this.contaBancariaOrigem = contaBancariaOrigem;
        this.contaBancariaDestino = contaBancariaDestino;
        this.data = data;
        this.valor = valor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getContaBancariaOrigem() {
        return contaBancariaOrigem;
    }

    public void setContaBancariaOrigem(int contaBancariaOrigem) {
        this.contaBancariaOrigem = contaBancariaOrigem;
    }

    public int getContaBancariaDestino() {
        return contaBancariaDestino;
    }

    public void setContaBancariaDestino(int contaBancariaDestino) {
        this.contaBancariaDestino = contaBancariaDestino;
    }

    public Timestamp getData() {
        return data;
    }

    public void setData(Timestamp data) {
        this.data = data;
    }

    public float getValor() {
        return valor;
    }

    public void setValor(float valor) {
        this.valor = valor;
    }
}
