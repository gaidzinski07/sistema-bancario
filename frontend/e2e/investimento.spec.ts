import { expect, test } from '@playwright/test';
import { SENHA_TRANSACAO, criarCliente, criarFundo, entrar, reais } from './apoio';

test('aplicação e resgate total ponta a ponta', async ({ page }) => {
  const fundo = await criarFundo(10, 100);
  const cliente = await criarCliente('Investidor E2E', 1000);

  await entrar(page, cliente.cpf);
  await page.goto('/investimentos/fundos');

  // aplicar R$ 300,00 a cota 10 = 30 cotas
  const cartao = page.locator('mat-card').filter({ hasText: fundo.nome });
  await cartao.getByRole('button', { name: 'Aplicar' }).click();
  const dialogo = page.getByRole('dialog');
  await dialogo.getByLabel('Valor').fill('300');
  await dialogo.getByLabel('Senha de transação').fill(SENHA_TRANSACAO);
  await dialogo.getByRole('button', { name: 'Aplicar' }).click();

  // vai para a posição
  await expect(page).toHaveURL(/\/investimentos\/posicoes\/\d+$/);
  await expect(page.getByRole('heading', { name: fundo.nome })).toBeVisible();
  await expect(page.getByText('30,00').first()).toBeVisible();
  await expect(page.getByRole('cell', { name: 'Aplicação' })).toBeVisible();

  // resgate total
  await page.getByRole('radio', { name: /Total/ }).check();
  await page.getByLabel('Senha de transação').fill(SENHA_TRANSACAO);
  await page.getByRole('button', { name: 'Resgatar para a conta corrente' }).click();

  const comprovante = page.getByRole('status').filter({ hasText: 'Resgate realizado' });
  await expect(comprovante).toBeVisible();
  await expect(comprovante).toContainText(reais(300));
  await expect(comprovante).toContainText(reais(1000));   // saldo da conta volta a 1.000,00
  // ao fechar o comprovante, a posição recarregada já aparece zerada
  await comprovante.getByRole('button', { name: 'OK' }).click();
  await expect(page.getByText('Esta posição foi totalmente resgatada.')).toBeVisible();

  // carteira vazia
  await page.goto('/investimentos');
  await expect(page.getByText('Você ainda não tem investimentos.')).toBeVisible();
});
