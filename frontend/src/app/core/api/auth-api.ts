import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Tokens, Usuario } from './modelos';

@Injectable({ providedIn: 'root' })
export class AuthApi {
  private readonly http = inject(HttpClient);

  login(cpf: string, senha: string): Observable<Tokens> {
    return this.http.post<Tokens>('/api/auth/login', { cpf, senha });
  }

  renovar(refreshToken: string): Observable<Tokens> {
    return this.http.post<Tokens>('/api/auth/refresh', { refreshToken });
  }

  logout(refreshToken: string): Observable<void> {
    return this.http.post<void>('/api/auth/logout', { refreshToken });
  }

  eu(): Observable<Usuario> {
    return this.http.get<Usuario>('/api/auth/me');
  }

  definirSenhaTransacao(senhaLogin: string, novaSenhaTransacao: string): Observable<void> {
    return this.http.put<void>('/api/auth/senha-transacao', { senhaLogin, novaSenhaTransacao });
  }
}
