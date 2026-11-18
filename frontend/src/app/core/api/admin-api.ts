import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Agencia,
  Cliente,
  Conta,
  Endereco,
  Fundo,
  Pagina,
  PerfilAdministrador,
  SegmentoCliente,
  TipoConta,
  Usuario,
} from './modelos';

export interface NovoCliente {
  nome: string;
  cpf: string;
  email: string | null;
  dataNascimento: string | null;
  senha: string;
  segmento: SegmentoCliente;
  endereco: Endereco | null;
}

export interface NovoAdministrador {
  nome: string;
  cpf: string;
  email: string | null;
  senha: string;
  perfil: PerfilAdministrador;
}

@Injectable({ providedIn: 'root' })
export class AdminApi {
  private readonly http = inject(HttpClient);

  clientes(pagina: number, tamanho: number): Observable<Pagina<Cliente>> {
    const params = new HttpParams({ fromObject: { pagina, tamanho } });
    return this.http.get<Pagina<Cliente>>('/api/admin/clientes', { params });
  }

  cliente(id: number): Observable<Cliente> {
    return this.http.get<Cliente>(`/api/admin/clientes/${id}`);
  }

  cadastrarCliente(cliente: NovoCliente): Observable<Cliente> {
    return this.http.post<Cliente>('/api/admin/clientes', cliente);
  }

  contasDoCliente(clienteId: number): Observable<Conta[]> {
    return this.http.get<Conta[]>(`/api/admin/clientes/${clienteId}/contas`);
  }

  abrirConta(clienteId: number, agenciaId: number, tipo: TipoConta): Observable<Conta> {
    return this.http.post<Conta>(`/api/admin/clientes/${clienteId}/contas`, { agenciaId, tipo });
  }

  agencias(): Observable<Agencia[]> {
    return this.http.get<Agencia[]>('/api/admin/agencias');
  }

  criarAgencia(codigo: string, nome: string, endereco: Endereco | null): Observable<Agencia> {
    return this.http.post<Agencia>('/api/admin/agencias', { codigo, nome, endereco });
  }

  fundos(): Observable<Fundo[]> {
    return this.http.get<Fundo[]>('/api/admin/fundos');
  }

  criarFundo(nome: string, cotaInicial: number, aplicacaoMinima: number): Observable<Fundo> {
    return this.http.post<Fundo>('/api/admin/fundos', { nome, cotaInicial, aplicacaoMinima });
  }

  atualizarCota(fundoId: number, valorCota: number): Observable<Fundo> {
    return this.http.put<Fundo>(`/api/admin/fundos/${fundoId}/cotacao`, { valorCota });
  }

  desativarFundo(fundoId: number): Observable<Fundo> {
    return this.http.delete<Fundo>(`/api/admin/fundos/${fundoId}`);
  }

  cadastrarAdministrador(admin: NovoAdministrador): Observable<Usuario> {
    return this.http.post<Usuario>('/api/admin/administradores', admin);
  }
}
