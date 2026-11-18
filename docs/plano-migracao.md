# Plano de migração — Sistema Bancário

**De:** Java 17 (fonte) / Java EE 7 (`javax.servlet`) + JSP + JDBC puro + Tomcat (projeto NetBeans/Ant)
**Para:** Java 21 + Spring Boot (Spring Data JPA/Hibernate, Flyway, Spring Security) + Angular (SPA)

> A análise abaixo considera o working tree atual, incluindo as alterações ainda não commitadas
> em `EmitirExtratoController`, `InvestimentoServlet`, `ContasBancariaDAO`, `InvestimentoDAO`,
> `visualizarInvestimentos.jsp` e `Cria_Tabelas.sql`.

---

## 1. Retrato do sistema atual

| Camada | O que existe |
|---|---|
| Build | NetBeans + Ant (`build.xml`, `nbproject/`), driver MySQL commitado em `web/driver/`, caminhos absolutos das máquinas de colegas em `nbproject/project.properties` |
| Web | 13 servlets em `src/java/` (pacote default), 2 filtros, JSPs com scriptlets, Bootstrap via CDN (5.0.2 **e** 5.3.2) **e** cópia local em `web/resource/bootstrap` |
| Dados | `model/*DAO` com JDBC puro, uma conexão nova por chamada (`model/Conexao.java`), credenciais `root/root` no código |
| Domínio | `entidade/*` são POJOs anêmicos com IDs inteiros no lugar de relacionamentos |
| Banco | MySQL. **Três** scripts de schema divergentes: `sql/Cria_Tabelas.sql` (o vigente), `sql/dbjava.sql` (schema antigo) e `web/sql/*` (outra variação, publicada como arquivo estático) |

### Funcionalidades (escopo de paridade)

1. Login de administrador (CPF + senha) e login de cliente (conta + senha)
2. Cadastro de usuário (admin)
3. Cadastro de conta bancária (admin) — **só existe a tela**, não está implementado
4. Depósito (cheque/dinheiro), saque (corrente/poupança), transferência (corrente/poupança/investimento)
5. Saldo e extrato
6. Fundos de investimento: listar, aplicar, ver carteira com rendimento, resgatar

### Modelo de dados vigente (`sql/Cria_Tabelas.sql`)

```
AGENCIA(id, endereco)
CONTA_BANCARIA(conta_corrente AI, id_agencia, saldo float, saldoPP float)   PK(conta_corrente, id_agencia)
USUARIO(id, cpf UNIQUE, nome, endereco, data_nascimento, senha)
ADMINISTRADOR(id_usuario PK/FK, tipo_administrador varchar)
CLIENTE(id_usuario PK/FK, tipo_cliente varchar, conta_bancaria UNIQUE FK)
FUNDO(id, nome, vr_cota, vr_minimo)
INVESTIMENTO(id, conta_bancaria, id_fundo, vr_cota_inicio, vr_investido, qtd_cotas, ts_investimento)
SAQUE(id, conta_bancaria, id_agencia, ts_saque, valor)
DEPOSITO(id, conta_bancaria, id_agencia, ts_deposito, valor, tipo int)
TRANSFERENCIA(id, conta_bancaria_origem, conta_bancaria_destino, ts_transferencia, valor)
```

---

## 2. Inventário de gambiarras e defeitos

Severidade: 🔴 crítico (dinheiro/segurança) · 🟠 bug funcional · 🟡 dívida técnica

### 2.1 Usuário "logado" fixo no código 🔴

As operações financeiras ignoram a sessão e sempre agem sobre o usuário/conta 1:

- [Realizardeposito.java:88](src/java/Realizardeposito.java:88) — `int id_logado = 1;`
- [Realizarsaque.java:89](src/java/Realizarsaque.java:89) — `int id_logado = 1;`
- [Realizartransferencia.java:116](src/java/Realizartransferencia.java:116) — `int id_logado = 1;` e [linha 148](src/java/Realizartransferencia.java:148) / [linha 55](src/java/Realizartransferencia.java:55) — `contaDao.get(1)`
- Até o commit `f2b3b06` o mesmo acontecia em investimentos/extrato (`getExtrato(2)`, `aplicar(...)` com conta `2` e agência `3` fixas). As alterações não commitadas corrigem extrato e aplicação, mas **resgate** ainda não valida o dono (ver 2.3).

### 2.2 Regras de negócio financeiras ausentes 🔴

- **Saque sem verificação de saldo** ([Realizarsaque.java:114-125](src/java/Realizarsaque.java:114)) — saldo pode ficar negativo.
- **Transferência sem verificação de saldo, sem validar valor positivo** — valor negativo "puxa" dinheiro do destinatário ([Realizartransferencia.java:157-162](src/java/Realizartransferencia.java:157)).
- **Agência e CPF do destinatário são lidos e ignorados** ([Realizartransferencia.java:127-143](src/java/Realizartransferencia.java:127)); `getUsuarioCPF` sempre lança exceção (lê coluna inexistente `ts_deposito`, [UsuarioDAO.java:247](src/java/model/UsuarioDAO.java:247)), que é engolida.
- **Transferência "de investimento" (Option 3) cria dinheiro do nada**: debita `vr_investido` via `InvestimentoDAO.update`, cujo SQL tem typo (`vr_cota_incio`, [InvestimentoDAO.java:116](src/java/model/InvestimentoDAO.java:116)) e falha silenciosamente, mas o crédito na poupança do destinatário é gravado. Também não registra `TRANSFERENCIA`.
- **Transferência de poupança é gravada igual à de corrente** — extrato não distingue; e o crédito cai na poupança do destino.
- **Depósito ignora o tipo de conta** — sempre credita `saldo` (corrente). O `tipo` (1=cheque, 2=dinheiro) é gravado como `int` mágico.
- **Saque/depósito na poupança não são distinguíveis no extrato**.
- **Resgate** ([InvestimentoDAO.java:207-219](src/java/model/InvestimentoDAO.java:207)): não confere se o investimento pertence à conta da sessão (qualquer cliente resgata o investimento de outro passando `idInvestimento`); checa `entidade == null` depois de já usá-la, e `get()` nunca retorna `null`; resgata só total.
- **Nada é transacional**: cada DAO abre sua própria conexão com autocommit. Débito, crédito e registro da transferência são 3 commits independentes; falha no meio deixa dinheiro sumido/duplicado.
- **Condição de corrida** em `atualizaSaldoAposOperacao` ([ContasBancariaDAO.java:148](src/java/model/ContasBancariaDAO.java:148)): lê saldo, soma em Java, grava — duas operações simultâneas perdem uma atualização.
- **`float` para dinheiro** (Java e SQL) — erro de arredondamento.
- `podeFazerOperacaoSaida` passa a conta com `setFloat` ([ContasBancariaDAO.java:132](src/java/model/ContasBancariaDAO.java:132)).

### 2.3 Segurança 🔴

- **Senhas em texto puro** no banco e na comparação (`senha.equals(...)`); seeds com `'123'`.
- **A tela de transferência mostra a senha digitada**: campo `type="text"` ([transferencia.jsp:89](web/transferencia.jsp:89)) e "Você digitou: <senha>" ([transferencia.jsp:95](web/transferencia.jsp:95)) — a servlet devolve a senha em `msgsenha`.
- **Senha logada no console** em [GenerateUserController.java:52](src/java/GenerateUserController.java:52).
- **Sem papéis**: admin e cliente gravam o mesmo atributo `usuario` na sessão. Os filtros só verificam "está logado", então qualquer cliente cria usuários.
- **Filtro de admin não protege nada**: `urlPatterns = {"/admin*"}` ([createBankAccountFilter.java:15](src/java/filtro/createBankAccountFilter.java:15)) é tratado como URL exata; o correto seria `/admin/*`.
- **Logout é um link para `login.jsp`** — a sessão nunca é invalidada.
- **XSS**: tudo é impresso com `<%= %>` sem escape, inclusive dentro de JavaScript (`popularModal(..., '<nome>')` em `realizarInvestimento.jsp`/`visualizarInvestimentos.jsp`, e `showMessage("<mensagem>")`).
- **Sem CSRF** em nenhum formulário POST.
- **IDOR** no resgate (2.2) e em qualquer operação que receba IDs do cliente.
- Credenciais do banco no código ([Conexao.java:15](src/java/model/Conexao.java:15)).
- Servlets de debug expostos em produção: `/Teste`, `/ConexaoTesteBD` (lista todas as contas/clientes).
- `web/sql/*.sql` é servido como estático e insere no schema de sistema `mysql.*`.

### 2.4 Bugs nos DAOs (código morto que quebra se for usado) 🟠

| Local | Defeito |
|---|---|
| [FundoDAO.java:55](src/java/model/FundoDAO.java:55) | `sql.setFloat(42, ...)` — índice inexistente |
| [FundoDAO.java:69](src/java/model/FundoDAO.java:69) | `UPDATE` usa colunas `valor_cota`/`valor_minimo` (não existem) |
| [DepositoDAO.java:90](src/java/model/DepositoDAO.java:90) | `UPDATE Deposito SET ... ts_saque` (coluna de outra tabela) |
| [AdministradorDAO.java:68](src/java/model/AdministradorDAO.java:68) | 3 placeholders, só 2 parâmetros |
| [TransferenciaDAO.java:95](src/java/model/TransferenciaDAO.java:95) | timestamp no lugar do `valor`, `setFloat` comentado |
| [ClienteDAO.java:65](src/java/model/ClienteDAO.java:65) | `INSERT` sem `id_usuario` (PK) |
| [UsuarioDAO.java:195](src/java/model/UsuarioDAO.java:195) | `SELECT * contasbancaria` sem `FROM`, tabela do schema antigo |
| [UsuarioDAO.java:113](src/java/model/UsuarioDAO.java:113), [:219](src/java/model/UsuarioDAO.java:219), [:247](src/java/model/UsuarioDAO.java:247) | leem `ts_deposito` da tabela `USUARIO` |
| [UsuarioDAO.java:260-277](src/java/model/UsuarioDAO.java:260) | conexão nunca fechada; `return` dentro do `finally` engole exceções |
| [InvestimentoDAO.java:46](src/java/model/InvestimentoDAO.java:46) | `get_CC` devolve só o último investimento da conta |
| `logar`/`logarCliente` | `SimpleDateFormat.parse(null)` se `data_nascimento` for nulo → login falha silenciosamente |
| Todos | `catch` que só faz `System.err.println` e retorna objeto vazio; "não encontrado" é `id == 0` |

### 2.5 Telas falsas ou quebradas 🟠

- **Saldo é mockado**: agência `0123`, conta `1234 5678 9843 1253` e saldo `R$85,00` fixos ([EmitirSaldoPage.jsp:54-62](web/EmitirSaldoPage.jsp:54)).
- `EmitirExtratoPage.jsp` é código morto (a servlet agora vai para `extrato.jsp`) e mostra o mesmo valor em "Entrada" e "Saída".
- **Criar conta bancária** não existe: formulário com `action=""` e `GenerateAccount` é o template do NetBeans.
- Login de cliente tem campo agência sem `name` (ignorado).
- `showMessage.jsp` carrega script de outro projeto (`/aplicacaoMVC/...`); URLs `http://localhost:8080/sistema-bancario/...` fixas em filtros, servlets e JSPs.
- Logo hospedada em `uploaddeimagens.com.br`; header duplicado em `index.html`, `home.jsp` e `HeaderComponent.html` (que não é incluído em lugar nenhum).

### 2.6 Schema e repositório 🟡

- `CONTA_BANCARIA` com PK composta `(conta_corrente, id_agencia)`, mas `conta_corrente` é auto-increment global e todas as FKs apontam só para `conta_corrente` — a agência na PK não tem função.
- Poupança modelada como coluna `saldoPP` na mesma linha da corrente.
- `CLIENTE` aponta para a conta (1:1 `UNIQUE`) em vez de a conta apontar para o titular → um cliente nunca terá duas contas.
- `ADMINISTRADOR.tipo_administrador` / `CLIENTE.tipo_cliente` como `varchar` livre; seeds tornam os usuários 1 e 2 admin **e** cliente.
- `Cria_Tabelas.sql`: comentários `--DROP` sem espaço (sintaxe inválida no MySQL), `DROP` de tabelas que não existem mais, e `ALTER TABLE investimento ADD ts_investimento` colado no final em vez de estar no `CREATE`.
- Extrato montado por `UNION ALL` de 4 tabelas.
- `.gitignore` lista `nbproject/private/*` e `project.properties`, mas eles estão versionados.
- Lixo: `model/dateFormat.java` (stub que lança `UnsupportedOperationException`), `entidade/Transacao.java` (não usado), `ConexaoTesteBD`, `Teste`, `EmitirSaldoController.processRequest` vazio, `doPost` vazio, imports não usados (`java.security.Timestamp`).

---

## 3. Arquitetura alvo

### 3.1 Decisões principais

| Tema | Decisão | Motivo |
|---|---|---|
| Runtime | Java 21 (LTS), Spring Boot na versão estável mais recente (Jakarta EE) | records, pattern matching, virtual threads disponíveis |
| Build | Maven (wrapper `mvnw`) | padrão de mercado, zero instalação local |
| Estilo | **Monólito modular**, pacotes por domínio (feature), não por camada técnica | o sistema é pequeno; microserviços seriam overengineering, mas a separação por módulo permite evoluir |
| Persistência | Spring Data JPA + Hibernate, MySQL 8, **Flyway** para versionar schema | substitui os 3 scripts divergentes por migrações reprodutíveis |
| Dinheiro | `BigDecimal` / `DECIMAL(19,2)`; cotas `DECIMAL(19,8)` | elimina `float` |
| Consistência | `@Transactional` nos serviços de aplicação; lock pessimista (`SELECT ... FOR UPDATE`) nas contas envolvidas, adquirido em ordem de ID para evitar deadlock; `@Version` como rede de segurança | resolve 2.2 (atomicidade e corrida) |
| Extrato | Tabela de **lançamentos** (ledger) gerada por toda operação | extrato vira uma consulta paginada simples, e o saldo é auditável |
| Segurança | Spring Security, BCrypt, papéis `ADMIN`/`CLIENTE`, **JWT** stateless (HS256, 15 min) + refresh token opaco (7 dias, hash no banco, rotação a cada uso, detecção de reuso, revogado no logout). Sem sessão e sem cookies, logo sem CSRF | decisão do grupo na Fase 2 (substituiu a proposta inicial de sessão em cookie). O refresh token revogável compensa o JWT não poder ser invalidado antes de expirar |
| Confirmação de operação | **Senha de transação** separada (hash), exigida em saque/transferência/aplicação/resgate | preserva a regra atual de "digite sua senha" sem reutilizar a senha de login |
| API | REST + DTOs `record`, Bean Validation, erros como `ProblemDetail` (RFC 9457) via `@RestControllerAdvice`, OpenAPI via springdoc | contrato explícito com o front |
| Front | Angular com standalone components, signals, reactive forms, rotas lazy, guards por papel | |
| Testes | JUnit 5, AssertJ, Testcontainers (MySQL real), `@DataJpaTest`, `@WebMvcTest`, ArchUnit para regras de dependência entre módulos; front com testes unitários + Playwright e2e | |
| Ambiente | `docker-compose` com MySQL; profiles `dev`/`test`/`prod`; config por variáveis de ambiente | remove `root/root` e caminhos absolutos |

### 3.2 Estrutura do repositório

```
sistema-bancario/
├── backend/                 Spring Boot
├── frontend/                Angular
├── docker-compose.yml       MySQL (+ backend/frontend opcionais)
├── docs/
└── .github/workflows/ci.yml
```

O código legado fica preservado numa tag (`legacy-jsp`) e é removido do `main` quando houver paridade.

### 3.3 Pacotes do backend

```
br.uff.banco
├── BancoApplication.java
├── shared/                         # código comum, sem regra de negócio
│   ├── domain/        Dinheiro (VO), Cpf (VO), EntidadeBase (id, criadoEm, atualizadoEm)
│   ├── web/           ApiExceptionHandler (ProblemDetail), PageResponse
│   └── security/      SecurityConfig, UsuarioAutenticado, CurrentUser resolver
├── identidade/                     # usuários, login, papéis
│   ├── api/           AuthController, UsuarioAdminController, dto/
│   ├── application/   AutenticacaoService, CadastroUsuarioService
│   ├── domain/        Usuario, Papel, PerfilAdministrador
│   └── infra/         UsuarioRepository, UserDetailsServiceImpl
├── clientes/
│   ├── api/ application/ domain/(Cliente, SegmentoCliente) infra/
├── agencias/
│   └── ...            Agencia
├── contas/                         # núcleo transacional
│   ├── api/           ContaController (saldo, extrato), OperacaoController (depósito, saque, transferência)
│   ├── application/   DepositoService, SaqueService, TransferenciaService, ExtratoQuery
│   ├── domain/        Conta, TipoConta, Lancamento, TipoLancamento, Deposito, Saque, Transferencia,
│   │                  SaldoInsuficienteException, ContaInativaException
│   └── infra/         ContaRepository (com @Lock), LancamentoRepository, ...
└── investimentos/
    ├── api/           FundoController, CarteiraController
    ├── application/   AplicacaoService, ResgateService, CotacaoService
    ├── domain/        Fundo, CotacaoFundo, Posicao (ex-Investimento), MovimentoInvestimento
    └── infra/
```

Regra (verificada com ArchUnit): `api → application → domain`; `infra` implementa portas do `domain`; módulos conversam via serviços de aplicação, nunca acessando repositório de outro módulo. Controllers não contêm regra de negócio; entidades têm comportamento (`conta.debitar(valor)` valida saldo e lança exceção de domínio), em vez do `get → soma em Java → update` atual.

### 3.4 Modelo de domínio e mapeamento ORM

```
Usuario (usuario)
  id, cpf UNIQUE, nome, email, dataNascimento (LocalDate), senhaHash, senhaTransacaoHash,
  papeis (@ElementCollection<Papel> ou tabela usuario_papel), ativo, criadoEm
  └─ 0..1 Cliente (cliente)          @OneToOne @MapsId → PK = usuario_id
  │       segmento (enum: CORRENTISTA, INVESTIDOR), endereco (@Embedded Endereco)
  │       └─ 1..* Conta
  └─ 0..1 PerfilAdministrador        enum GERENTE/OPERADOR (coluna em usuario ou tabela própria)

Agencia (agencia)
  id, codigo (4 dígitos) UNIQUE, endereco (@Embedded)

Conta (conta)
  id (surrogate), agencia @ManyToOne, numero, digito, UNIQUE(agencia_id, numero)
  titular @ManyToOne Cliente, tipo (enum CORRENTE, POUPANCA), saldo DECIMAL(19,2),
  status (ATIVA/BLOQUEADA/ENCERRADA), version (@Version)

Lancamento (lancamento)                        # extrato
  id, conta @ManyToOne, tipo (enum DEPOSITO, SAQUE, TRANSFERENCIA_ENVIADA, TRANSFERENCIA_RECEBIDA,
  APLICACAO, RESGATE), natureza (CREDITO/DEBITO), valor (sempre positivo), saldoApos,
  descricao, contraparte (nome/conta), operacaoId (referência à operação), ocorridoEm
  INDEX(conta_id, ocorrido_em)

Deposito    (deposito)       id, conta, forma (enum CHEQUE, DINHEIRO), valor, agencia, ocorridoEm
Saque       (saque)          id, conta, valor, agencia, ocorridoEm
Transferencia (transferencia) id, origem @ManyToOne Conta, destino @ManyToOne Conta, valor,
                             chaveIdempotencia UNIQUE, ocorridoEm

Fundo (fundo)                id, nome, cotaAtual DECIMAL(19,8), aplicacaoMinima, ativo
CotacaoFundo (cotacao_fundo) id, fundo, data, valorCota          # histórico, alimenta rendimento
Posicao (posicao)            id, conta, fundo, quantidadeCotas, valorAplicado, cotaMedia, abertaEm
MovimentoInvestimento        id, posicao, tipo (APLICACAO/RESGATE), cotas, valorCota, valor, ocorridoEm
```

Mudanças deliberadas em relação ao legado:

- **Poupança vira uma `Conta` com `tipo = POUPANCA`** em vez da coluna `saldoPP`. Movimentar entre corrente e poupança do mesmo cliente passa a ser uma transferência comum.
- **Conta aponta para o titular** (`conta.titular_id`), permitindo várias contas por cliente.
- **PK surrogate** em `conta`; número de conta + agência viram chave natural única (evita `@IdClass`/`@EmbeddedId` sem necessidade).
- **Papéis** substituem as tabelas `ADMINISTRADOR`/`CLIENTE` como mecanismo de autorização; `Cliente` continua existindo como entidade de negócio.
- **`Investimento` → `Posicao` + `MovimentoInvestimento`**: permite resgate parcial e várias aplicações no mesmo fundo; o rendimento sai de `quantidadeCotas × cotaAtual − valorAplicado`.
- **Enums** (`@Enumerated(EnumType.STRING)`) no lugar de `int`/`varchar` mágicos.
- Datas em `LocalDate`/`Instant` (colunas `DATE`/`TIMESTAMP(6)` em UTC).
- `FetchType.LAZY` em todos os `@ManyToOne`; consultas de leitura (extrato, carteira) com projeções DTO/`@Query` para evitar N+1.

### 3.5 Fluxo transacional (exemplo: transferência)

```java
@Transactional
public ComprovanteTransferencia transferir(UsuarioAutenticado usuario, TransferenciaCommand cmd) {
    senhaTransacao.validar(usuario, cmd.senhaTransacao());
    idempotencia.verificar(cmd.chaveIdempotencia());

    var origem  = contas.buscarDoTitular(cmd.contaOrigemId(), usuario.clienteId());   // 404/403 se não for dele
    var destino = contas.buscarPorAgenciaENumero(cmd.agenciaDestino(), cmd.contaDestino());
    destino.titular().exigirCpf(cmd.cpfDestino());                                   // valida o que hoje é ignorado

    var bloqueadas = contas.bloquearEmOrdem(origem.id(), destino.id());              // SELECT ... FOR UPDATE
    var transferencia = Transferencia.entre(origem, destino, Dinheiro.of(cmd.valor())); // valida > 0, origem ≠ destino
    origem.debitar(transferencia.valor());                                           // lança SaldoInsuficienteException
    destino.creditar(transferencia.valor());

    transferencias.save(transferencia);
    lancamentos.registrar(transferencia);                                            // 2 lançamentos: débito e crédito
    return ComprovanteTransferencia.de(transferencia);
}
```

Tudo é commitado junto ou nada é — o oposto dos 3 commits soltos de `Realizartransferencia`.

### 3.6 API REST (paridade com o legado)

| Legado | Novo endpoint | Papel |
|---|---|---|
| `LoginController` / `ClienteController` (POST) | `POST /api/auth/login` (CPF + senha, para os dois papéis), `POST /api/auth/refresh`, `POST /api/auth/logout`, `GET /api/auth/me` | público / autenticado |
| `CreateUser` | `POST /api/admin/clientes`, `GET /api/admin/clientes`, `GET /api/admin/clientes/{id}`, `POST /api/admin/administradores` | ADMIN |
| `admin/createBankAccountPage.jsp` (não implementado) | `POST /api/admin/clientes/{id}/contas`, `GET /api/admin/clientes/{id}/contas` | ADMIN |
| — | `GET/POST /api/admin/agencias`, `POST /api/admin/fundos`, `PUT /api/admin/fundos/{id}/cotacao` | ADMIN |
| `EmitirSaldoController` (mock) | `GET /api/contas` (minhas contas com saldo), `GET /api/contas/{id}` | CLIENTE |
| `EmitirExtratoController` | `GET /api/contas/{id}/extrato?de=&ate=&page=` | CLIENTE |
| `Realizardeposito` | `POST /api/contas/{id}/depositos` | CLIENTE |
| `Realizarsaque` | `POST /api/contas/{id}/saques` | CLIENTE |
| `Realizartransferencia` | `POST /api/transferencias` (header `Idempotency-Key`) | CLIENTE |
| `FundoServlet` | `GET /api/fundos` | autenticado |
| `InvestimentoServlet?acao=APLICACAO` | `POST /api/investimentos/aplicacoes` | CLIENTE |
| `InvestimentoServlet` (GET) | `GET /api/investimentos/posicoes` | CLIENTE |
| `InvestimentoServlet?acao=RESGATE` | `POST /api/investimentos/posicoes/{id}/resgates` (valor ou total) | CLIENTE |
| `Teste`, `ConexaoTesteBD` | removidos (`/actuator/health` no lugar) | — |

Toda rota de cliente obtém a conta **a partir do usuário autenticado** e valida a titularidade; nenhum ID vindo do cliente é confiado sem essa verificação.

### 3.7 Frontend Angular

```
frontend/src/app/
├── core/          auth.service (guarda o access token em memória e renova via refresh), auth.guard, role.guard,
│                  http interceptors (Bearer, erro → toast, 401 → tenta refresh uma vez → login),
│                  layout (header/nav únicos, substituindo as 3 cópias)
├── shared/        componentes de UI, pipes (moeda BRL, CPF, conta/agência), validators (CPF, valor > 0)
├── features/
│   ├── auth/          login
│   ├── dashboard/     resumo: contas e saldos reais
│   ├── contas/        extrato (filtro por período, paginação)
│   ├── operacoes/     deposito, saque, transferencia (com etapa de confirmação + senha de transação)
│   ├── investimentos/ fundos, carteira (rendimento), aplicar/resgatar em dialog
│   └── admin/         usuarios, clientes/contas, agencias, fundos/cotações
└── app.routes.ts  rotas lazy por feature, guardadas por papel
```

- Biblioteca de UI: **Angular Material** (ou ng-bootstrap, para manter a identidade visual atual em Bootstrap). Fim das duas versões de Bootstrap via CDN mais a cópia local.
- Interpolação do Angular escapa HTML por padrão, o que elimina o XSS dos scriptlets.
- Cliente HTTP tipado gerado do OpenAPI do backend (`openapi-generator`), ou escrito à mão com interfaces espelhando os DTOs.
- Em desenvolvimento: `ng serve` com `proxy.conf.json` para `/api`. Em produção: build estático servido pelo próprio Spring Boot (mesmo domínio, sem CORS) ou por Nginx na frente.

---

## 4. Plano de execução por fases

Cada fase termina com algo executável e testado. Backend e frontend podem andar em paralelo a partir da Fase 2, desde que o contrato (OpenAPI) da feature esteja definido.

### Fase 0 — Preparação
- Commitar ou descartar o WIP atual; criar a tag `legacy-jsp`.
- Validar com o grupo as **decisões de negócio abertas** (seção 5).
- Ajustar `.gitignore` (tirar `nbproject/private` e `project.properties` do índice) e remover `web/driver/*.jar`.

### Fase 1 — Fundação do backend
- Gerar projeto Spring Boot (Web, Data JPA, Validation, Security, Flyway, MySQL, Actuator, springdoc, Testcontainers).
- `docker-compose.yml` com MySQL 8; `application.yml` com profiles e variáveis de ambiente.
- `V1__schema_inicial.sql` (modelo da seção 3.4), `V2__seed_dev.sql` só no profile `dev` (Flyway `locations` por profile).
- `ApiExceptionHandler` com `ProblemDetail`, VOs `Dinheiro` e `Cpf`, `EntidadeBase`.
- CI (GitHub Actions): build + testes com Testcontainers.
- **Pronto quando:** `./mvnw verify` passa, a aplicação sobe contra o MySQL do compose e `/actuator/health` responde.

### Fase 2 — Identidade e cadastros
- `Usuario`, `Cliente`, `Agencia`, `Conta` (sem operações ainda) + repositórios + `@DataJpaTest`.
- Spring Security: login por CPF e senha (BCrypt), JWT + refresh token com rotação, papéis, logout que revoga o refresh token.
- Endpoints de admin: criar usuário/cliente, abrir conta (corrente/poupança) — implementando o que hoje é só tela.
- **Pronto quando:** testes provam que CLIENTE recebe 403 em `/api/admin/**` e anônimo recebe 401 em tudo, menos no login.

### Fase 3 — Núcleo transacional
- `Conta.debitar/creditar`, `Lancamento`, depósito, saque, transferência com lock, validação de titularidade, idempotência e senha de transação.
- Testes: unidade de domínio (saldo insuficiente, valor ≤ 0, mesma conta); integração com Testcontainers; **teste de concorrência** (N threads sacando da mesma conta não deixam o saldo negativo nem perdem atualização).
- **Pronto quando:** todos os defeitos de 2.1 e 2.2 têm teste que reproduz o cenário e passa.

### Fase 4 — Saldo e extrato
- ~~`GET /api/contas`~~ (entregue na Fase 3), extrato paginado sobre `lancamento` com filtro de período.
- **Pronto quando:** a soma dos lançamentos bate com o saldo da conta (teste de invariante).

### Fase 5 — Investimentos
- `Fundo`, `CotacaoFundo`, `Posicao`, `MovimentoInvestimento`; aplicar (valida mínimo e saldo, debita a conta corrente), resgatar parcial/total (credita a conta corrente); atualização de cota pelo admin.
- Remover a "transferência a partir do investimento" (Option 3): o fluxo correto é resgate → conta → transferência.
- **Pronto quando:** rendimento calculado bate com cotas × cota atual e o resgate de posição alheia retorna 404/403.

### Fase 6 — Frontend Angular
- Setup (`ng new`, lint, testes, proxy), layout, auth (login/logout/guards/interceptors).
- Telas na ordem do backend: dashboard → extrato → depósito/saque/transferência → investimentos → admin.
- Playwright: login, transferência ponta a ponta, resgate.
- **Pronto quando:** todas as funcionalidades da seção 1 funcionam pelo Angular, sem nenhuma JSP.

### Fase 7 — Corte e limpeza
- Remover `src/`, `web/`, `nbproject/`, `build.xml`, `sql/` do `main` (permanecem na tag `legacy-jsp`).
- README com como subir (compose), usuários de seed, arquitetura e decisões (ADRs curtos em `docs/adr/`).
- Opcional: Dockerfile multi-stage (build do Angular embutido no jar), observabilidade (logs estruturados, métricas do Actuator).

### Migração de dados

O banco atual só tem dados de seed, então **não há migração de dados**: o schema novo nasce no Flyway e os seeds são reescritos no modelo novo (senhas com BCrypt). Se houver um banco com dados que valha preservar, criar uma migração `V3__importa_legado.sql` que leia do schema antigo: `saldo` → conta CORRENTE, `saldoPP` → conta POUPANCA, depósitos/saques/transferências → operações + lançamentos, investimentos → posições.

---

## Status

- **Fase 1 — concluída (2026-11-16).** Backend em `backend/` (Spring Boot 4.1.2, Java 21). A V1 do schema
  foi escrita assumindo as opções *recomendadas* das decisões 1, 2, 3, 4 e 5 abaixo (poupança como
  conta separada, resgate parcial etc.). O seed de desenvolvimento torna o usuário 1 admin + cliente,
  como no legado (decisão 7 ainda em aberto). Se o grupo decidir diferente, o ajuste entra como `V2`.
  Além do MySQL 8 (alvo), o backend roda contra MariaDB local via `jdbc:mariadb://`.
- **Fase 2 — concluída (2026-11-18).** Entidades `Usuario`, `Cliente` (`@MapsId`), `Agencia`, `Conta` e
  `RefreshToken`; migration `V2` (refresh tokens e numeração de contas por agência, com lock pessimista).
  Autenticação por **JWT** (decisão do grupo, no lugar da sessão em cookie). Endpoints de admin para
  agências, clientes, contas e administradores. Coleção do Postman em `docs/postman/`.
  Fica para fases seguintes: troca de senha, definição da senha de transação pelo cliente (Fase 3),
  limite de tentativas de login e restrição de quem pode criar administradores (hoje, qualquer ADMIN).
- **Fase 3 — concluída (2026-11-18).** Depósito, saque e transferência com lock pessimista (ordem por
  ID entre duas contas), lançamento gerado junto de toda mudança de saldo, titularidade pelo token,
  idempotência por `Idempotency-Key` e senha de transação (4–6 dígitos, bloqueio de 15 min após 3
  erros; migration `V3`). Testes reproduzem os defeitos de 2.1–2.3 e a concorrência (saques
  simultâneos, transferências cruzadas, mesma chave em paralelo). `GET /api/contas` (minhas contas com
  saldo) foi antecipado da Fase 4, porque o cliente precisa dos IDs para operar.
  Decisões assumidas: cheque creditado na hora (decisão 6 em aberto); sem limites diários (decisão 9).

## 5. Decisões de negócio a confirmar antes da Fase 1

1. **Poupança como conta separada** (recomendado) ou sub-saldo da mesma conta?
2. Transferência entre corrente e poupança do mesmo titular é permitida (recomendado: sim, como transferência comum)?
3. Remover a transferência direta a partir do investimento (recomendado: sim; usa resgate)?
4. Resgate parcial (recomendado: sim) e resgate sempre para a conta corrente?
5. Destino da transferência identificado por agência + conta, com CPF como confirmação (recomendado) ou só pelo CPF?
6. Depósito em cheque: credita na hora (como hoje) ou fica "em compensação" até o admin liberar?
7. Admin também pode ser cliente (como nos seeds atuais) ou os papéis são exclusivos?
8. Quem atualiza a cota dos fundos: admin manualmente (como hoje, direto no banco) ou um job agendado?
9. Limites (saque diário, valor máximo de transferência) entram no escopo?
