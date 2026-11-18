# banco-frontend

Frontend do sistema bancário — Angular 22 (componentes standalone, signals, zoneless) com Angular Material 22.
Consome a API do [`backend`](../backend/README.md). Arquitetura e roadmap: [`docs/plano-migracao.md`](../docs/plano-migracao.md).

## Pré-requisitos

- Node **22.23.5** (versão em [`.nvmrc`](.nvmrc); com nvm: `nvm install 22.23.5`). Aceita também Node 24.15+ e 26+.
- API no ar em `http://localhost:8080` (ver README do backend).

## Rodando

```bash
npm ci
npm start            # http://localhost:4200 — /api é repassado para a API pelo proxy.conf.json
```

Usuários de teste: os do seed de desenvolvimento (credenciais no cabeçalho de
`backend/src/main/resources/db/seed/V1_1__seed_dev.sql`).

## Testes

```bash
npx ng test --watch=false    # unitários (Vitest + jsdom)
npx playwright test          # ponta a ponta (Chromium); exige a API no ar com o profile dev
```

Os testes ponta a ponta criam os próprios dados pela API (cliente, conta, senha de transação, fundo),
então podem rodar repetidas vezes sem depender do estado do banco.

## Estrutura

```
src/app/
├── core/
│   ├── api/       clientes HTTP tipados e modelos (espelham os records do backend)
│   ├── auth/      Sessao (JWT), interceptor (Bearer + renovação), guards por papel
│   ├── erros/     leitura do ProblemDetail do backend, avisos (snackbar)
│   └── layout/    shell (barra + menu lateral por papel), textos do Material em português
├── shared/        pipes e validadores (CPF, conta, tipos), comprovante
└── features/      telas, carregadas sob demanda por área
    ├── auth/              login
    ├── inicio/            contas, saldos e atalhos
    ├── contas/            extrato por período
    ├── operacoes/         depósito, saque, transferência, senha de transação
    ├── investimentos/     fundos, carteira, posição e resgate
    └── admin/             clientes, contas, agências, fundos/cotas, administradores
```

## Decisões

- **Sessão JWT**: access token só em memória; refresh token no `sessionStorage` (sobrevive a F5 na mesma
  aba, some ao fechá-la). O interceptor renova uma única vez quando a API responde 401 — e renovações
  simultâneas compartilham a mesma chamada, porque o refresh token é de uso único e o backend derruba a
  sessão se um token já usado reaparecer.
- **Transferência idempotente**: a `Idempotency-Key` é gerada na confirmação e reaproveitada em
  "Tentar de novo" após erro de rede; mudar qualquer dado gera outra.
- **Erros**: o texto exibido é o `detail` do `ProblemDetail` do backend; erros por campo (400) vão para
  os `mat-error` do formulário.
- **Autorização**: as rotas são escondidas por papel só por conveniência; quem garante é o backend (401/403).
- **Formatos**: locale `pt-BR`, moeda BRL, datas da API em `AAAA-MM-DD`/ISO-8601 (instantes em UTC,
  exibidos no fuso do navegador).
