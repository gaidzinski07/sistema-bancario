import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ComprovanteOperacao,
  ComprovanteTransferencia,
  Conta,
  Extrato,
  FormaDeposito,
} from './modelos';

export interface NovaTransferencia {
  contaOrigemId: number;
  agenciaDestino: string;
  contaDestino: string;
  cpfDestino: string;
  valor: number;
  senhaTransacao: string;
}

export interface FiltroExtrato {
  de: string;
  ate: string;
  pagina: number;
  tamanho: number;
}

@Injectable({ providedIn: 'root' })
export class ContasApi {
  private readonly http = inject(HttpClient);

  minhas(): Observable<Conta[]> {
    return this.http.get<Conta[]>('/api/contas');
  }

  detalhe(contaId: number): Observable<Conta> {
    return this.http.get<Conta>(`/api/contas/${contaId}`);
  }

  extrato(contaId: number, filtro: FiltroExtrato): Observable<Extrato> {
    const params = new HttpParams({ fromObject: { ...filtro } });
    return this.http.get<Extrato>(`/api/contas/${contaId}/extrato`, { params });
  }

  depositar(contaId: number, valor: number, forma: FormaDeposito, senhaTransacao: string): Observable<ComprovanteOperacao> {
    return this.http.post<ComprovanteOperacao>(`/api/contas/${contaId}/depositos`, { valor, forma, senhaTransacao });
  }

  sacar(contaId: number, valor: number, senhaTransacao: string): Observable<ComprovanteOperacao> {
    return this.http.post<ComprovanteOperacao>(`/api/contas/${contaId}/saques`, { valor, senhaTransacao });
  }

  /**
   * A mesma `chaveIdempotencia` deve ser reenviada se a primeira tentativa falhar por
   * rede/timeout: o backend devolve o comprovante original em vez de transferir de novo.
   */
  transferir(pedido: NovaTransferencia, chaveIdempotencia: string): Observable<ComprovanteTransferencia> {
    const headers = new HttpHeaders({ 'Idempotency-Key': chaveIdempotencia });
    return this.http.post<ComprovanteTransferencia>('/api/transferencias', pedido, { headers });
  }
}
