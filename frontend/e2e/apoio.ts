import { APIRequestContext, Page, expect, request } from '@playwright/test';

/** API de verdade, chamada direto (sem o proxy do ng serve) para montar os dados de cada teste. */
export const API_URL = process.env['API_URL'] ?? 'http://localhost:8080';

/** Credenciais do administrador do seed de desenvolvimento (V1_1__seed_dev.sql). */
const ADMIN = { cpf: '12345678909', senha: 'Senha@123' };

export const SENHA_LOGIN = 'SenhaE2E123';
export const SENHA_TRANSACAO = '2468';

export interface ClienteDeTeste {
  id: number;
  nome: string;
  cpf: string;
  conta: { id: number; agencia: string; numeroComDigito: string };
}

/** CPF válido aleatório (dígitos verificadores calculados). */
export function novoCpf(): string {
  const d = Array.from({ length: 9 }, () => Math.floor(Math.random() * 10));
  const digito = (n: number) => {
    const soma = d.slice(0, n).reduce((total, valor, i) => total + valor * (n + 1 - i), 0);
    const resto = (soma * 10) % 11;
    return resto === 10 ? 0 : resto;
  };
  d.push(digito(9));
  d.push(digito(10));
  const cpf = d.join('');
  return /^(\d)\1{10}$/.test(cpf) ? novoCpf() : cpf;
}

async function token(api: APIRequestContext, cpf: string, senha: string): Promise<string> {
  const resposta = await api.post('/api/auth/login', { data: { cpf, senha } });
  expect(resposta.ok(), await resposta.text()).toBeTruthy();
  return (await resposta.json()).accessToken;
}

async function apiComo(tokenDeAcesso: string): Promise<APIRequestContext> {
  return request.newContext({ baseURL: API_URL, extraHTTPHeaders: { Authorization: `Bearer ${tokenDeAcesso}` } });
}

/** Cliente novo com conta corrente, senha de transação definida e (opcionalmente) saldo. */
export async function criarCliente(nome: string, saldoInicial = 0): Promise<ClienteDeTeste> {
  const anonimo = await request.newContext({ baseURL: API_URL });
  const admin = await apiComo(await token(anonimo, ADMIN.cpf, ADMIN.senha));
  const cpf = novoCpf();

  const cliente = await (await admin.post('/api/admin/clientes', {
    data: { nome, cpf, senha: SENHA_LOGIN, segmento: 'CORRENTISTA' },
  })).json();
  const conta = await (await admin.post(`/api/admin/clientes/${cliente.id}/contas`, {
    data: { agenciaId: 1, tipo: 'CORRENTE' },
  })).json();

  const doCliente = await apiComo(await token(anonimo, cpf, SENHA_LOGIN));
  const senha = await doCliente.put('/api/auth/senha-transacao', {
    data: { senhaLogin: SENHA_LOGIN, novaSenhaTransacao: SENHA_TRANSACAO },
  });
  expect(senha.status()).toBe(204);
  if (saldoInicial > 0) {
    const deposito = await doCliente.post(`/api/contas/${conta.id}/depositos`, {
      data: { valor: saldoInicial, forma: 'DINHEIRO', senhaTransacao: SENHA_TRANSACAO },
    });
    expect(deposito.status()).toBe(201);
  }
  await Promise.all([anonimo.dispose(), admin.dispose(), doCliente.dispose()]);

  return { id: cliente.id, nome, cpf, conta: { id: conta.id, agencia: conta.agencia, numeroComDigito: `${conta.numero}-${conta.digito}` } };
}

/** Fundo novo e exclusivo do teste (a cota pode ser alterada sem afetar outros testes). */
export async function criarFundo(cota: number, aplicacaoMinima: number): Promise<{ id: number; nome: string }> {
  const anonimo = await request.newContext({ baseURL: API_URL });
  const admin = await apiComo(await token(anonimo, ADMIN.cpf, ADMIN.senha));
  const nome = `Fundo E2E ${Date.now()}`;
  const fundo = await (await admin.post('/api/admin/fundos', { data: { nome, cotaInicial: cota, aplicacaoMinima } })).json();
  await Promise.all([anonimo.dispose(), admin.dispose()]);
  return { id: fundo.id, nome };
}

/**
 * Faz login pela tela. Por padrão espera chegar ao início: um `page.goto` logo depois recarregaria
 * a página antes de a sessão ser gravada.
 */
export async function entrar(pagina: Page, cpf: string, senha = SENHA_LOGIN, { esperarSucesso = true } = {}): Promise<void> {
  await pagina.goto('/login');
  await pagina.getByLabel('CPF').fill(cpf);
  await pagina.getByLabel('Senha', { exact: true }).fill(senha);
  await pagina.getByRole('button', { name: 'Entrar' }).click();
  if (esperarSucesso) {
    await expect(pagina).toHaveURL(/\/inicio$/);
  }
}

/** "1000" → "R$ 1.000,00" (como o CurrencyPipe em pt-BR, com espaço não separável). */
export function reais(valor: number): RegExp {
  const texto = valor.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  return new RegExp(`R\\$\\s${texto.replace('.', '\\.')}`);
}
