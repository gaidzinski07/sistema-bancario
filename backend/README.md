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

## Profiles

| Profile | Uso |
|---|---|
| `dev` (padrão) | banco local, carrega `db/seed` (usuários e contas de exemplo — credenciais no cabeçalho de `V1_1__seed_dev.sql`) |
| `prod` | tudo vem de `DB_URL`/`DB_USER`/`DB_PASSWORD`, sem seed, sem Swagger |

## Testes

```bash
./mvnw verify
```

- `*Test` — unitários, fatias MVC e regras de arquitetura (ArchUnit); rodam em qualquer máquina.
- `*IT` — sobem a aplicação contra um MySQL real via Testcontainers. **Sem Docker são pulados**; no CI sempre rodam.

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
