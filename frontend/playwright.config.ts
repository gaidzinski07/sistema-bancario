import { defineConfig, devices } from '@playwright/test';

/**
 * Testes ponta a ponta: navegador de verdade contra o front e a API de verdade.
 *
 * A API precisa estar no ar em `API_URL` (padrão http://localhost:8080) com o profile `dev`
 * (o seed traz o admin usado para criar os dados de cada teste). Localmente:
 *   docker compose up -d mysql && (cd backend && ./mvnw spring-boot:run)
 * No CI, o workflow sobe MySQL e API antes.
 *
 * Por padrão o front vem do `ng serve` (porta 4200). Com `BASE_URL` definido, os testes rodam
 * contra um front já no ar — ex.: a imagem Docker, que serve o Angular pela própria API:
 *   docker compose --profile completo up -d && BASE_URL=http://localhost:8080 npx playwright test
 */
const BASE_URL = process.env['BASE_URL'];

export default defineConfig({
  testDir: 'e2e',
  fullyParallel: false,
  forbidOnly: !!process.env['CI'],
  retries: process.env['CI'] ? 1 : 0,
  reporter: process.env['CI'] ? [['github'], ['html', { open: 'never' }]] : 'list',
  use: {
    baseURL: BASE_URL ?? 'http://localhost:4200',
    locale: 'pt-BR',
    timezoneId: 'America/Sao_Paulo',
    trace: 'retain-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: BASE_URL
    ? undefined
    : {
        command: 'npx ng serve --port 4200',
        url: 'http://localhost:4200',
        reuseExistingServer: !process.env['CI'],
        timeout: 120_000,
      },
});
