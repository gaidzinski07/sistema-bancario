/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

import java.sql.Timestamp;

/**
 *
 * @author João
 */
public class TransacaoDTO {
    private String tipo;
    private int id;
    private Timestamp data;
    private float valor;
    private String nome;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome == null ? "--" : nome;
    }

    public TransacaoDTO() {
    }

    public TransacaoDTO(String tipo, int id, Timestamp data, float valor, String nome) {
        this.tipo = tipo;
        this.id = id;
        this.data = data;
        this.valor = valor;
        setNome(nome);
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
