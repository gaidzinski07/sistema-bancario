/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

import entidade.Fundo;
import entidade.Investimento;

/**
 *
 * @author João
 */
public class InvestimentoDTO {
    private Fundo fundo;
    private Investimento investimento;
    private float rendimentos = 0;

    public Fundo getFundo() {
        return fundo;
    }

    public void setFundo(Fundo fundo) {
        this.fundo = fundo;
    }

    public Investimento getInvestimento() {
        return investimento;
    }

    public void setInvestimento(Investimento investimento) {
        this.investimento = investimento;
    }

    public InvestimentoDTO() {
    }

    public float getRendimentos() {
        return rendimentos;
    }

    public void setRendimentos(float vrCotaIni, float vrCotaAtual) {
        this.rendimentos = vrCotaAtual / vrCotaIni;
    }

    public InvestimentoDTO(Fundo fundo, Investimento investimento) {
        this.fundo = fundo;
        this.investimento = investimento;
        float vrCotaIni = investimento.getVrCotaInicio();
        float vrCotaAtual = fundo.getValorCota();
        setRendimentos(vrCotaIni, vrCotaAtual);
    }
}
