/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entidade;

import java.util.Date;
import entidade.Transferencia;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author João
 */
public class Transferencia {
    private int idTransferencia;
    private int idUsuarioOrigem;
    private int idUsuarioDestino;
    private Date data;
    private float valor;

    public Transferencia() {
    }

    public Transferencia(int id_transferencia, int id_usuario_origem, int id_usuario_destino, Date data, float valor) {
        this.idTransferencia = id_transferencia;
        this.idUsuarioOrigem = id_usuario_origem;
        this.idUsuarioDestino = id_usuario_destino;
        this.data = data;
        this.valor = valor;
    }

    public int getIdTransferencia() {
        return idTransferencia;
    }

    public void setIdTransferencia(int idTransferencia) {
        this.idTransferencia = idTransferencia;
    }

    public int getIdUsuarioOrigem() {
        return idUsuarioOrigem;
    }

    public void setIdUsuarioOrigem(int idUsuarioOrigem) {
        this.idUsuarioOrigem = idUsuarioOrigem;
    }

    public int getIdUsuarioDestino() {
        return idUsuarioDestino;
    }

    public void setIdUsuarioDestino(int idUsuarioDestino) {
        this.idUsuarioDestino = idUsuarioDestino;
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
