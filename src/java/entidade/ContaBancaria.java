/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entidade;

/**
 *
 * @author lucas
 * 
 */
public class ContaBancaria {

    private int id;
    private int contaCorrente;
    private int contaPoupanca;
    private int idCliente;
    private float saldoAtual;
    private int idAgencia;
    
    public ContaBancaria() {
    }

    public ContaBancaria(int id, int contaCorrente, int contaPoupanca, int idCliente, float saldoAtual, int idAgencia) {
        this.id = id;
        this.contaCorrente = contaCorrente;
        this.contaPoupanca = contaPoupanca;
        this.idCliente = idCliente;
        this.saldoAtual = saldoAtual;
        this.idAgencia = idAgencia;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setContaCorrente(int contaCorrente) {
        this.contaCorrente = contaCorrente;
    }

    public void setContaPoupanca(int contaPoupanca) {
        this.contaPoupanca = contaPoupanca;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public void setSaldoAtual(float saldoAtual) {
        this.saldoAtual = saldoAtual;
    }

    public void setIdAgencia(int idAgencia) {
        this.idAgencia = idAgencia;
    }


    public int getId() {
        return id;
    }

    public int getContaCorrente() {
        return contaCorrente;
    }

    public int getContaPoupanca() {
        return contaPoupanca;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public float getSaldoAtual() {
        return saldoAtual;
    }

    public int getIdAgencia() {
        return idAgencia;
    }

}
