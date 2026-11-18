import { expect, test } from '@playwright/test';
import { SENHA_TRANSACAO, criarCliente, entrar, reais } from './apoio';

test('transferência ponta a ponta: dados, confirmação com senha, comprovante e extrato', async ({ page }) => {
  const origem = await criarCliente('Origem E2E', 1000);
  const destino = await criarCliente('Destino E2E');

  await entrar(page, origem.cpf);
  await page.getByRole('link', { name: 'Transferir' }).first().click();
  await expect(page).toHaveURL(/\/operacoes\/transferencia$/);

  await page.getByLabel('Conta de origem').click();
  await page.getByRole('option', { name: new RegExp(origem.conta.numeroComDigito) }).click();
  await page.getByLabel('Agência').fill(destino.conta.agencia);
  await page.getByLabel('Conta com dígito').fill(destino.conta.numeroComDigito);
  await page.getByLabel('CPF do titular').fill(destino.cpf);
  await page.getByLabel('Valor').fill('150.00');
  await page.getByRole('button', { name: 'Continuar' }).click();

  await expect(page.getByText(/Transferir\s+R\$\s150,00/)).toBeVisible();
  await page.getByLabel('Senha de transação').fill(SENHA_TRANSACAO);
  await page.getByRole('button', { name: 'Confirmar transferência' }).click();

  const comprovante = page.getByRole('status').filter({ hasText: 'Transferência realizada' });
  await expect(comprovante).toBeVisible();
  await expect(comprovante).toContainText('Favorecido: Destino E2E');
  await expect(comprovante).toContainText(reais(850));

  await page.getByRole('link', { name: 'Ver extrato' }).click();
  await expect(page.getByRole('cell', { name: /Transferência enviada/ })).toBeVisible();
  await expect(page.getByText(reais(850)).first()).toBeVisible();
});

test('senha de transação errada não transfere', async ({ page }) => {
  const origem = await criarCliente('Origem Senha E2E', 100);
  const destino = await criarCliente('Destino Senha E2E');

  await entrar(page, origem.cpf);
  await page.goto('/operacoes/transferencia');
  await page.getByLabel('Conta de origem').click();
  await page.getByRole('option').first().click();
  await page.getByLabel('Agência').fill(destino.conta.agencia);
  await page.getByLabel('Conta com dígito').fill(destino.conta.numeroComDigito);
  await page.getByLabel('CPF do titular').fill(destino.cpf);
  await page.getByLabel('Valor').fill('10');
  await page.getByRole('button', { name: 'Continuar' }).click();
  await page.getByLabel('Senha de transação').fill('0000');
  await page.getByRole('button', { name: 'Confirmar transferência' }).click();

  await expect(page.getByRole('alert')).toContainText('Senha de transação incorreta');
  await page.goto('/inicio');
  await expect(page.getByText(reais(100)).first()).toBeVisible();
});
