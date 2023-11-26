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
    private int id;
    private int contaBancaria;
    private int idFundo;
    private float vrCotaInicio;
    private float vrInvestido;
    private float qtdCotas;

    public Investimento() {
    }

    public Investimento(int id, int contaBancaria, int id_fundo, float vrCotaInicio, float vrInvestido, float qtdCotas) {
        this.id = id;
        this.contaBancaria = contaBancaria;
        this.idFundo = id_fundo;
        this.vrCotaInicio = vrCotaInicio;
        this.vrInvestido = vrInvestido;
        this.qtdCotas = qtdCotas;
    }

    public int getId() {
        return id;
    }

    public void setId(int idInvestimento) {
        this.id = idInvestimento;
    }

    public int getContaBancaria() {
        return contaBancaria;
    }

    public void setContaBancaria(int contaBancaria) {
        this.contaBancaria = contaBancaria;
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
