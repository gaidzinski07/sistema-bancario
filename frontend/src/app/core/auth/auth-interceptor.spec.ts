import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { Tokens } from '../api/modelos';
import { authInterceptor } from './auth-interceptor';
import { CHAVE_REFRESH, Sessao } from './sessao';

const TOKENS: Tokens = { accessToken: 'access-1', tipo: 'Bearer', expiraEmSegundos: 900, refreshToken: 'refresh-1' };

describe('authInterceptor', () => {
  let cliente: HttpClient;
  let http: HttpTestingController;
  let sessao: Sessao;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting(), provideRouter([])],
    });
    cliente = TestBed.inject(HttpClient);
    http = TestBed.inject(HttpTestingController);
    sessao = TestBed.inject(Sessao);
    // sessão aberta com access-1 / refresh-1
    sessao.login('x', 'y').subscribe();
    http.expectOne('/api/auth/login').flush(TOKENS);
    http.expectOne('/api/auth/me').flush({ id: 1, nome: 'A', papeis: ['CLIENTE'] });
  });

  afterEach(() => http.verify());

  it('envia o access token como Bearer', () => {
    cliente.get('/api/contas').subscribe();
    expect(http.expectOne('/api/contas').request.headers.get('Authorization')).toBe('Bearer access-1');
  });

  it('não envia token nas rotas de autenticação', () => {
    cliente.post('/api/auth/refresh', {}).subscribe();
    expect(http.expectOne('/api/auth/refresh').request.headers.has('Authorization')).toBe(false);
  });

  it('com 401, renova pelo refresh token e repete a chamada com o novo token', () => {
    let resposta: unknown;
    cliente.get('/api/contas').subscribe((r) => (resposta = r));

    http.expectOne('/api/contas').flush({ codigo: 'token-invalido', status: 401 }, { status: 401, statusText: 'Unauthorized' });
    http.expectOne('/api/auth/refresh').flush({ ...TOKENS, accessToken: 'access-2', refreshToken: 'refresh-2' });
    const repetida = http.expectOne('/api/contas');
    expect(repetida.request.headers.get('Authorization')).toBe('Bearer access-2');
    repetida.flush([{ id: 1 }]);

    expect(resposta).toEqual([{ id: 1 }]);
  });

  it('duas chamadas com 401 ao mesmo tempo disparam um único refresh', () => {
    cliente.get('/api/contas').subscribe();
    cliente.get('/api/investimentos/posicoes').subscribe();

    http.expectOne('/api/contas').flush(null, { status: 401, statusText: 'Unauthorized' });
    http.expectOne('/api/investimentos/posicoes').flush(null, { status: 401, statusText: 'Unauthorized' });
    http.expectOne('/api/auth/refresh').flush({ ...TOKENS, accessToken: 'access-2', refreshToken: 'refresh-2' });

    http.expectOne('/api/contas').flush([]);
    http.expectOne('/api/investimentos/posicoes').flush([]);
  });

  it('se a renovação falhar, encerra a sessão e manda para o login', () => {
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    let erro: unknown;
    cliente.get('/api/contas').subscribe({ error: (e) => (erro = e) });

    http.expectOne('/api/contas').flush(null, { status: 401, statusText: 'Unauthorized' });
    http.expectOne('/api/auth/refresh').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(erro).toBeTruthy();
    expect(sessao.autenticado()).toBe(false);
    expect(sessionStorage.getItem(CHAVE_REFRESH)).toBeNull();
    expect(navegar).toHaveBeenCalledWith(['/login'], { queryParams: { sessao: 'expirada' } });
  });

  it('outros erros passam direto, sem renovar', () => {
    let status = 0;
    cliente.get('/api/contas/99').subscribe({ error: (e) => (status = e.status) });
    http.expectOne('/api/contas/99').flush({ codigo: 'nao-encontrado', status: 404 }, { status: 404, statusText: 'Not Found' });

    http.expectNone('/api/auth/refresh');
    expect(status).toBe(404);
  });
});
