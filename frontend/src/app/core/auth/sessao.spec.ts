import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { Tokens, Usuario } from '../api/modelos';
import { CHAVE_REFRESH, Sessao } from './sessao';

const TOKENS: Tokens = { accessToken: 'access-1', tipo: 'Bearer', expiraEmSegundos: 900, refreshToken: 'refresh-1' };
const NOVOS_TOKENS: Tokens = { accessToken: 'access-2', tipo: 'Bearer', expiraEmSegundos: 900, refreshToken: 'refresh-2' };
const CLIENTE: Usuario = {
  id: 3, nome: 'Luiz Gustavo', cpf: '390.533.447-05', email: null, dataNascimento: null,
  papeis: ['CLIENTE'], perfilAdministrador: null, ativo: true,
};

describe('Sessao', () => {
  let sessao: Sessao;
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    sessao = TestBed.inject(Sessao);
    http = TestBed.inject(HttpTestingController);
    // sem rotas de verdade no teste: a navegação para /login só é registrada
    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
  });

  afterEach(() => http.verify());

  it('login guarda o access token em memória, o refresh na aba e carrega o usuário', () => {
    let usuario: Usuario | undefined;
    sessao.login('39053344705', 'Senha@123').subscribe((u) => (usuario = u));

    http.expectOne('/api/auth/login').flush(TOKENS);
    http.expectOne('/api/auth/me').flush(CLIENTE);

    expect(usuario).toEqual(CLIENTE);
    expect(sessao.autenticado()).toBe(true);
    expect(sessao.ehCliente()).toBe(true);
    expect(sessao.ehAdmin()).toBe(false);
    expect(sessao.accessToken()).toBe('access-1');
    expect(sessionStorage.getItem(CHAVE_REFRESH)).toBe('refresh-1');
  });

  it('renovações simultâneas usam uma única chamada (o refresh token é de uso único)', () => {
    sessionStorage.setItem(CHAVE_REFRESH, 'refresh-1');
    const recebidos: string[] = [];

    sessao.renovar().subscribe((t) => recebidos.push(t));
    sessao.renovar().subscribe((t) => recebidos.push(t));
    sessao.renovar().subscribe((t) => recebidos.push(t));

    const requisicao = http.expectOne('/api/auth/refresh');
    expect(requisicao.request.body).toEqual({ refreshToken: 'refresh-1' });
    requisicao.flush(NOVOS_TOKENS);

    expect(recebidos).toEqual(['access-2', 'access-2', 'access-2']);
    expect(sessionStorage.getItem(CHAVE_REFRESH)).toBe('refresh-2');
  });

  it('depois de concluída, a próxima renovação faz uma nova chamada', () => {
    sessionStorage.setItem(CHAVE_REFRESH, 'refresh-1');
    sessao.renovar().subscribe();
    http.expectOne('/api/auth/refresh').flush(NOVOS_TOKENS);

    sessao.renovar().subscribe();
    expect(http.expectOne('/api/auth/refresh').request.body).toEqual({ refreshToken: 'refresh-2' });
  });

  it('falha na renovação encerra a sessão', () => {
    sessionStorage.setItem(CHAVE_REFRESH, 'refresh-1');
    let falhou = false;
    sessao.renovar().subscribe({ error: () => (falhou = true) });

    http.expectOne('/api/auth/refresh').flush({ status: 401, codigo: 'nao-autenticado' }, { status: 401, statusText: 'Unauthorized' });

    expect(falhou).toBe(true);
    expect(sessao.accessToken()).toBeNull();
    expect(sessionStorage.getItem(CHAVE_REFRESH)).toBeNull();
  });

  it('restaurar sem refresh token na aba não chama a API', async () => {
    await sessao.restaurar();
    http.expectNone('/api/auth/refresh');
    expect(sessao.autenticado()).toBe(false);
  });

  it('restaurar reabre a sessão da aba (F5)', async () => {
    sessionStorage.setItem(CHAVE_REFRESH, 'refresh-1');
    const restaurando = sessao.restaurar();

    http.expectOne('/api/auth/refresh').flush(NOVOS_TOKENS);
    await Promise.resolve();
    http.expectOne('/api/auth/me').flush(CLIENTE);
    await restaurando;

    expect(sessao.usuario()).toEqual(CLIENTE);
  });

  it('sair revoga o refresh token e limpa a sessão', () => {
    sessao.login('39053344705', 'Senha@123').subscribe();
    http.expectOne('/api/auth/login').flush(TOKENS);
    http.expectOne('/api/auth/me').flush(CLIENTE);

    sessao.sair();

    expect(http.expectOne('/api/auth/logout').request.body).toEqual({ refreshToken: 'refresh-1' });
    expect(sessao.autenticado()).toBe(false);
    expect(sessionStorage.getItem(CHAVE_REFRESH)).toBeNull();
  });
});
