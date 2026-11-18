import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ComprovanteInvestimento, Cotacao, DetalhePosicao, Fundo, Posicao } from './modelos';

/** Resgate parcial (`valor`) ou total (`total: true`). */
export type PedidoResgate = { valor: number } | { total: true };

@Injectable({ providedIn: 'root' })
export class InvestimentosApi {
  private readonly http = inject(HttpClient);

  fundos(): Observable<Fundo[]> {
    return this.http.get<Fundo[]>('/api/fundos');
  }

  cotacoes(fundoId: number): Observable<Cotacao[]> {
    return this.http.get<Cotacao[]>(`/api/fundos/${fundoId}/cotacoes`);
  }

  aplicar(contaId: number, fundoId: number, valor: number, senhaTransacao: string): Observable<ComprovanteInvestimento> {
    return this.http.post<ComprovanteInvestimento>('/api/investimentos/aplicacoes', { contaId, fundoId, valor, senhaTransacao });
  }

  carteira(): Observable<Posicao[]> {
    return this.http.get<Posicao[]>('/api/investimentos/posicoes');
  }

  posicao(posicaoId: number): Observable<DetalhePosicao> {
    return this.http.get<DetalhePosicao>(`/api/investimentos/posicoes/${posicaoId}`);
  }

  resgatar(posicaoId: number, pedido: PedidoResgate, senhaTransacao: string): Observable<ComprovanteInvestimento> {
    return this.http.post<ComprovanteInvestimento>(`/api/investimentos/posicoes/${posicaoId}/resgates`, {
      ...pedido,
      senhaTransacao,
    });
  }
}
