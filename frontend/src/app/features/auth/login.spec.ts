import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { Login } from './login';

describe('Login', () => {
  let fixture: ComponentFixture<Login>;
  let http: HttpTestingController;
  let navegar: ReturnType<typeof vi.spyOn>;

  function entrar(cpf: string, senha: string): void {
    const elemento: HTMLElement = fixture.nativeElement;
    const [campoCpf, campoSenha] = Array.from(elemento.querySelectorAll('input'));
    campoCpf.value = cpf;
    campoCpf.dispatchEvent(new Event('input'));
    campoSenha.value = senha;
    campoSenha.dispatchEvent(new Event('input'));
    elemento.querySelector('form')!.dispatchEvent(new Event('submit'));
  }

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    fixture = TestBed.createComponent(Login);
    http = TestBed.inject(HttpTestingController);
    navegar = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  function responderLoginOk(): void {
    http.expectOne('/api/auth/login').flush({ accessToken: 'a', tipo: 'Bearer', expiraEmSegundos: 900, refreshToken: 'r' });
    http.expectOne('/api/auth/me').flush({ id: 1, nome: 'Luiz', papeis: ['CLIENTE'] });
  }

  it('CPF inválido nem chega a chamar a API', () => {
    entrar('111.111.111-11', 'qualquer');
    http.expectNone('/api/auth/login');
  });

  it('login certo vai para o início', () => {
    entrar('390.533.447-05', 'Senha@123');
    responderLoginOk();
    expect(navegar).toHaveBeenCalledWith('/inicio');
  });

  it('volta para o destino guardado pelo guard', () => {
    fixture.componentRef.setInput('destino', '/contas/5/extrato');
    entrar('390.533.447-05', 'Senha@123');
    responderLoginOk();
    expect(navegar).toHaveBeenCalledWith('/contas/5/extrato');
  });

  it.each(['https://site-malicioso.com', '//site-malicioso.com'])('ignora destino externo %s (open redirect)', (destino) => {
    fixture.componentRef.setInput('destino', destino);
    entrar('390.533.447-05', 'Senha@123');
    responderLoginOk();
    expect(navegar).toHaveBeenCalledWith('/inicio');
  });

  it('mostra a mensagem do backend quando as credenciais não conferem', async () => {
    entrar('390.533.447-05', 'errada');
    http.expectOne('/api/auth/login').flush(
      { status: 401, codigo: 'nao-autenticado', detail: 'CPF ou senha inválidos.' },
      { status: 401, statusText: 'Unauthorized' },
    );
    await fixture.whenStable();
    expect((fixture.nativeElement as HTMLElement).querySelector('[role=alert]')?.textContent).toContain('CPF ou senha inválidos.');
  });
});
