import { expect, test } from '@playwright/test';
import { ClienteDeTeste, criarCliente, entrar, reais } from './apoio';

test.describe('login', () => {
  let cliente: ClienteDeTeste;

  test.beforeAll(async () => {
    cliente = await criarCliente('Cliente Login E2E', 1000);
  });

  test('cliente entra, vê a conta com o saldo real e sai', async ({ page }) => {
    await entrar(page, cliente.cpf);

    await expect(page).toHaveURL(/\/inicio$/);
    await expect(page.getByRole('heading', { name: 'Olá, Cliente' })).toBeVisible();
    await expect(page.getByText(cliente.conta.numeroComDigito)).toBeVisible();
    await expect(page.getByText(reais(1000)).first()).toBeVisible();
    // cliente não vê o menu de administração
    await expect(page.getByText('Administração')).toHaveCount(0);

    await page.getByRole('button', { name: 'Sair' }).click();
    await expect(page).toHaveURL(/\/login$/);
  });

  test('senha errada mostra a mensagem do backend', async ({ page }) => {
    await entrar(page, cliente.cpf, 'senha-errada', { esperarSucesso: false });

    await expect(page.getByRole('alert')).toHaveText('CPF ou senha inválidos.');
    await expect(page).toHaveURL(/\/login$/);
  });

  test('a sessão sobrevive a um recarregamento da página (refresh token)', async ({ page }) => {
    await entrar(page, cliente.cpf);
    await expect(page).toHaveURL(/\/inicio$/);

    await page.reload();

    await expect(page).toHaveURL(/\/inicio$/);
    await expect(page.getByText(cliente.conta.numeroComDigito)).toBeVisible();
  });

  test('rota interna sem sessão vai para o login e volta depois', async ({ page }) => {
    await page.goto(`/contas/${cliente.conta.id}/extrato`);
    await expect(page).toHaveURL(/\/login\?destino=/);

    await page.getByLabel('CPF').fill(cliente.cpf);
    await page.getByLabel('Senha', { exact: true }).fill('SenhaE2E123');
    await page.getByRole('button', { name: 'Entrar' }).click();

    await expect(page).toHaveURL(new RegExp(`/contas/${cliente.conta.id}/extrato$`));
  });
});
