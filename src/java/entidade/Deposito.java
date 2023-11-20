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
public class Deposito {
    
//  CREATE TABLE IF NOT EXISTS `Deposito` (
//  `id_deposito` int NOT NULL,
//  `id_usuario_destino` int NOT NULL,
//  `data` timestamp NOT NULL,
//  `local` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
//  `id_agencia` int NOT NULL,
//  `valor` float NOT NULL,
//   PRIMARY KEY (`id_deposito`),
//   KEY `FK_user_dep` (`id_usuario_destino`),
//   KEY `FK_sg` (id_agencia) 
//  ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
    
    private int id;
    private int usuarioDestino;
    private Date dataDeposito;
    private String local;
    private int idAgencia;
    private float valor;

    public Deposito() {
    }
    
    public Deposito(int id, int usuarioDestino, Date dataDeposito, String local, int idAgencia, float valor) {
        this.id = id;
        this.usuarioDestino = usuarioDestino;
        this.dataDeposito = dataDeposito;
        this.local = local;
        this.idAgencia = idAgencia;
        this.valor = valor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioDestino() {
        return usuarioDestino;
    }

    public void setUsuarioDestino(int usuarioDestino) {
        this.usuarioDestino = usuarioDestino;
    }

    public Date getDataDeposito() {
        return dataDeposito;
    }

    public void setDataDeposito(Date dataDeposito) {
        this.dataDeposito = dataDeposito;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
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
}
