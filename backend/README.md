# banco-backend

API REST do sistema bancário — Java 21, Spring Boot 4.1, Spring Data JPA (Hibernate 7), Flyway, Spring Security.
Arquitetura e roadmap: [`docs/plano-migracao.md`](../docs/plano-migracao.md).

## Pré-requisitos

- JDK 21 (`JAVA_HOME` apontando para ele — o Maven vem pelo wrapper `./mvnw`)
- Um banco: **MySQL 8** (alvo, via Docker) ou **MariaDB** local (ex.: XAMPP)

## Subindo

### Com Docker (MySQL 8 — mesmo banco do CI)

```bash
docker compose up -d mysql        # na raiz do repositório; MySQL na porta 3307
./mvnw spring-boot:run            # profile dev: aplica migrations + dados de exemplo
```

### Com MySQL/MariaDB já instalado

Use variáveis de ambiente (o driver é escolhido pelo prefixo da URL):

```bash
DB_URL='jdbc:mariadb://localhost:3306/banco?createDatabaseIfNotExist=true' DB_USER=root DB_PASSWORD=root ./mvnw spring-boot:run
```

Para MySQL, troque o prefixo por `jdbc:mysql://`.

### Endpoints úteis

| URL | |
|---|---|
| `GET /actuator/health` | health check (público) |
| `/swagger-ui.html` | documentação da API (desligada em `prod`) |

Coleção do Postman: [`docs/postman/sistema-bancario.postman_collection.json`](../docs/postman/sistema-bancario.postman_collection.json).

## Autenticação (JWT)

1. `POST /api/auth/login` com `{"cpf": "...", "senha": "..."}` → `accessToken` (JWT, 15 min) + `refreshToken` (7 dias).
2. Demais chamadas: header `Authorization: Bearer <accessToken>`.
3. Expirou? `POST /api/auth/refresh` com `{"refreshToken": "..."}` → novo par. O refresh token é **de uso único**;
   reapresentar um já usado revoga a sessão inteira (proteção contra token vazado).
4. `POST /api/auth/logout` com o refresh token. O access token continua válido até expirar — o cliente deve descartá-lo.

| Rotas | Acesso |
|---|---|
| `/api/auth/login`, `/refresh`, `/logout` | público |
| `/api/admin/**` | papel `ADMIN` |
| `/api/contas/**`, `/api/transferencias` | papel `CLIENTE` |
| demais `/api/**` | qualquer usuário autenticado |

Erros de autenticação/autorização seguem o mesmo `ProblemDetail` do resto da API (`401` com
`codigo: nao-autenticado` ou `token-invalido`; `403` com `codigo: acesso-negado`).

## Operações do cliente

| Endpoint | |
|---|---|
| `PUT /api/auth/senha-transacao` | define a senha de transação (4–6 dígitos); exige a senha de login |
| `GET /api/contas` | minhas contas, com saldo |
| `GET /api/contas/{id}` | detalhe de uma conta |
| `GET /api/contas/{id}/extrato?de=&ate=&pagina=&tamanho=` | extrato do período (padrão: últimos 30 dias) |
| `POST /api/contas/{id}/depositos` | `{"valor", "forma": "DINHEIRO"\|"CHEQUE", "senhaTransacao"}` |
| `POST /api/contas/{id}/saques` | `{"valor", "senhaTransacao"}` |
| `POST /api/transferencias` | header `Idempotency-Key` + `{"contaOrigemId", "agenciaDestino", "contaDestino": "00003-5", "cpfDestino", "valor", "senhaTransacao"}` |

- Toda operação exige a **senha de transação**. Após 3 erros seguidos ela fica bloqueada por 15 minutos
  (redefinir pelo `PUT` desbloqueia).
- A conta tem que ser do cliente do token; conta de outra pessoa responde 404.
- Valores com mais de 2 casas decimais são recusados (400), não arredondados.
- Transferência: agência, conta com dígito e CPF do titular do destino têm que bater. Reenviar com a mesma
  `Idempotency-Key` devolve o comprovante original (200) sem transferir de novo.
- Extrato: `de`/`ate` são dias de calendário no fuso `America/Sao_Paulo` (configurável em
  `banco.fuso-horario`), ambos inclusive, até 366 dias. Saldos e totais são do período inteiro, e vale
  sempre `saldoInicial + totalCreditos − totalDebitos = saldoFinal`. Os instantes vêm em UTC (ISO-8601).
- Por baixo: lock pessimista nas contas (em ordem de ID quando são duas), operação + saldo + lançamento
  na mesma transação, e `@Version` como segunda proteção contra atualização perdida.

## Profiles

| Profile | Uso |
|---|---|
| `dev` (padrão) | banco local, carrega `db/seed` (usuários e contas de exemplo — credenciais no cabeçalho de `V1_1__seed_dev.sql`) e usa uma chave JWT fixa de desenvolvimento |
| `prod` | tudo vem do ambiente: `DB_URL`/`DB_USER`/`DB_PASSWORD` e `JWT_SECRET` (Base64, ≥ 256 bits); sem seed, sem Swagger |

Para gerar um `JWT_SECRET`:

```bash
openssl rand -base64 48
```

## Testes

```bash
./mvnw verify
```

- `*Test` — unitários, fatias MVC e regras de arquitetura (ArchUnit); rodam em qualquer máquina.
- `*IT` — sobem a aplicação contra um MySQL real via Testcontainers. **Sem Docker são pulados**; no CI sempre rodam.
  Compartilham um único container/contexto (`@TesteDeIntegracao`), então cada teste cria seus próprios dados.

## Estrutura

```
br.uff.banco
├── shared/          # sem regra de negócio
│   ├── domain/      # Dinheiro, Cpf, exceções de domínio
│   ├── persistence/ # EntidadeBase, conversores JPA
│   ├── security/    # SecurityConfig
│   └── web/         # ApiExceptionHandler (ProblemDetail / RFC 9457)
└── <módulo>/        # identidade, clientes, agencias, contas, investimentos (Fases 2–5)
    ├── api/         # controllers + DTOs
    ├── application/ # serviços transacionais (casos de uso)
    ├── domain/      # entidades e regras
    └── infra/       # repositórios
```

Migrations ficam em `src/main/resources/db/migration` e **nunca são editadas depois de aplicadas** num
ambiente compartilhado — mudanças de schema entram como uma nova versão (`V2__...`).
