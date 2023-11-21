/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entidade;

/**
 *
 * @author João
 */
public class Investimento {
    private int idInvestimento;
    private int idCliente;
    private int idFundo;
    private float vrCotaInicio;
    private float vrInvestido;
    private float qtdCotas;

    public Investimento() {
    }

    public Investimento(int id_investimento, int id_cliente, int id_fundo, float vrCotaInicio, float vrInvestido, float qtdCotas) {
        this.idInvestimento = id_investimento;
        this.idCliente = id_cliente;
        this.idFundo = id_fundo;
        this.vrCotaInicio = vrCotaInicio;
        this.vrInvestido = vrInvestido;
        this.qtdCotas = qtdCotas;
    }

    public int getIdInvestimento() {
        return idInvestimento;
    }

    public void setIdInvestimento(int idInvestimento) {
        this.idInvestimento = idInvestimento;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdFundo() {
        return idFundo;
    }

    public void setIdFundo(int idFundo) {
        this.idFundo = idFundo;
    }

    public float getVrCotaInicio() {
        return vrCotaInicio;
    }

    public void setVrCotaInicio(float vrCotaInicio) {
        this.vrCotaInicio = vrCotaInicio;
    }

    public float getVrInvestido() {
        return vrInvestido;
    }

    public void setVrInvestido(float vrInvestido) {
        this.vrInvestido = vrInvestido;
    }

    public float getQtdCotas() {
        return qtdCotas;
    }

    public void setQtdCotas(float qtdCotas) {
        this.qtdCotas = qtdCotas;
    }
    
}
