# TaskFlow

TaskFlow é um aplicativo Android de gerenciamento de tarefas recorrentes, construído
com **Clean Architecture**, **MVVM + StateFlow** e um design system próprio
("Liquid Glass"). O app cria lembretes que se repetem automaticamente (diária,
semanal, mensal, anual ou personalizada), organiza tudo numa Home com carrosséis por
categoria, e dispara notificações locais precisas mesmo em Doze Mode — 100% offline.

## Índice

- [Funcionalidades](#-funcionalidades)
- [Arquitetura](#%EF%B8%8F-arquitetura)
- [Design System — Liquid Glass](#-design-system--liquid-glass)
- [Home — carrosséis por categoria](#-home--carross%C3%A9is-por-categoria)
- [Notificações](#-notifica%C3%A7%C3%B5es)
- [Motion & microinterações](#-motion--microintera%C3%A7%C3%B5es)
- [Acessibilidade](#-acessibilidade)
- [Tecnologias](#-tecnologias)
- [Testes](#-testes)
- [CI/CD](#-cicd)
- [Como executar](#-como-executar)
- [Decisões de arquitetura e trade-offs](#-decis%C3%B5es-de-arquitetura-e-trade-offs)

## 🎯 Funcionalidades

- **Tarefas recorrentes**: diária, semanal, mensal, anual ou intervalo customizado em dias
- **Categorização** por cor/ícone (Pets, Casa, Saúde, Manutenção, ...) — com categorias
  seedadas por padrão e criação de categorias personalizadas direto na tela de Nova Tarefa
- **Edição de tarefas**, reaproveitando a mesma tela de criação
- **Notificações locais exatas**: `AlarmManager` (com `setExactAndAllowWhileIdle`) para
  lembretes no horário certo, com fallback automático para `WorkManager` quando o
  usuário não concede a permissão de alarmes exatos, e reagendamento automático após
  reiniciar o aparelho (`BootCompletedReceiver`)
- **Tema claro/escuro persistente** via DataStore, com toggle na tela principal, cada
  um com sua própria identidade visual (ver [Design System](#-design-system--liquid-glass))
- **Home organizada em carrosséis por categoria** (estilo Shellbox), com **reordenação
  por arraste** e banner de resumo semanal — ver seção dedicada abaixo
- **Concluir/desfazer com feedback claro**: ao marcar uma tarefa como feita, um
  Snackbar mostra a próxima data de vencimento, com opção de desfazer
- **100% offline**, com Room + migrations versionadas (sem perda de dados entre updates)

## 🏗️ Arquitetura

Clean Architecture em 3 camadas, dentro de um único módulo `:app` (ver
[Decisões de arquitetura](#-decis%C3%B5es-de-arquitetura-e-trade-offs) sobre por que
não modularizamos por enquanto):

```
com.taskflow.app/
├── domain/                       # Kotlin puro — nenhuma dependência de Android/Compose
│   ├── model/                    # Task, Category, TaskExecution, TaskCompletionResult, RecurrenceType
│   ├── repository/                # Interfaces (portas de saída)
│   ├── scheduler/                  # TaskNotificationScheduler (porta — Dependency Inversion)
│   ├── usecase/                     # 1 responsabilidade de negócio por classe
│   └── util/                         # RecurrenceCalculator (regra de negócio pura)
│
├── data/
│   ├── local/
│   │   ├── database/              # Room: AppDatabase, entities, DAOs, Migrations, seed
│   │   ├── preferences/            # ThemeManager (DataStore)
│   │   └── repository/              # Implementações das interfaces de domain.repository
│   └── mapper/                       # Entity <-> Domain (um arquivo por entidade)
│
├── notification/                  # Implementações concretas do TaskNotificationScheduler
│   ├── AlarmScheduler.kt           # Lembretes exatos via AlarmManager
│   ├── NotificationScheduler.kt     # Decide Alarm vs WorkManager, implementa a porta de domínio
│   ├── BootCompletedReceiver.kt      # Reagenda alarmes perdidos após reiniciar o aparelho
│   ├── TaskReminderReceiver.kt        # Recebe o alarme e delega ao Worker
│   └── TaskNotificationWorker.kt       # Constrói/exibe a notificação (única fonte de verdade)
│
├── presentation/
│   ├── model/                      # TaskUiModel (@Immutable, fora do domínio)
│   ├── util/                        # DateFormats (única fonte de verdade de formatação)
│   ├── designsystem/                 # Liquid Glass (ver seção própria)
│   ├── theme/                         # ThemeViewModel (consome data.local.preferences.ThemeManager)
│   ├── components/                     # TaskCarouselItem, HomeSummaryBanner, DragDropListState
│   ├── tasklist/ · addtask/ · taskdetail/   # ViewModel + State + Screen por feature
│
└── di/                              # Koin: AppModule (data/domain) e PresentationModule
```

**Fluxo de dados (unidirecional):**

```
UI Event → ViewModel → UseCase → Repository (porta) → RepositoryImpl → Room
                                                              │
ViewModel ←── StateFlow ←── domain.model.Task ←──────────────┘
     │
     └─→ mapeado para presentation.model.TaskUiModel na borda de apresentação
```

Por que `TaskNotificationScheduler` é uma interface dentro de `domain.scheduler`
em vez de o domínio importar `notification.NotificationScheduler` diretamente?
Porque isso violaria a regra de dependência da Clean Architecture (as camadas internas
não podem depender de detalhes de framework). A implementação concreta —
que decide entre `AlarmManager` e `WorkManager` — é injetada via Koin.

**Regra de negócio centralizada, não duplicada:** `ScheduleNotificationUseCase` decide
sozinho se deve agendar (`task.notificationEnabled`) — os quatro use cases que o chamam
(`AddTaskUseCase`, `UpdateTaskUseCase`, `CompleteTaskUseCase`, `UndoCompleteTaskUseCase`)
não repetem essa checagem. Esse tipo de duplicação é exatamente o que uma auditoria de
arquitetura deve caçar: regra de negócio espalhada em vários lugares é regra de negócio
que vai divergir silenciosamente na próxima mudança.

## 🎨 Design System — Liquid Glass

Todos os componentes vivem em `presentation/designsystem/` e compartilham a mesma
paleta e vocabulário visual (gradientes translúcidos + bordas com brilho sutil):

| Componente | Uso |
|---|---|
| `LiquidBackground` | Fundo animado (blobs de cor + bolhas + grain), usado em toda tela |
| `GlassCard` | Container translúcido com borda gradiente — base de cards e banners |
| `GlassButton` | Botão primário com gradiente horizontal `GlassPrimary → GlassSecondary` |
| `GlassTextField` | Input com fundo translúcido e cor de foco `GlassPrimary` |
| `GlassChip` | Seleção única (recorrência, categoria), com `semantics { role = RadioButton }` |

**Duas identidades visuais, um só vocabulário:** claro e escuro não são só um
inverte-cores um do outro — têm paletas propositalmente diferentes. O que os mantém
como "o mesmo app" é a estrutura do Liquid Glass (gradiente translúcido + borda com
brilho + grain sutil) ser idêntica; só a cor por baixo muda. Por isso
`GlassPrimary`/`GlassSecondary`/`GlassTertiary` (`Theme.kt`) são propriedades
computadas a partir do `ColorScheme` ativo — não constantes fixas — e todo o resto
do design system lê a cor **através delas**, nunca de um valor hexadecimal direto.

| Token | Claro (bege/marrom/rosa pastel) | Escuro (preto/neon) | Uso |
|---|---|---|---|
| `GlassPrimary` | `#D98CA0` (rosa empoeirado) | `#39FF88` (verde neon) | Ações primárias, ícones ativos |
| `GlassSecondary` | `#A9714A` (caramelo) | `#FF3DAE` (rosa neon) | Gradientes, `LiquidBackground` |
| `GlassTertiary` | `#C1476B` (framboesa) | `#FF1F5A` (rosa-alerta) | Estados de alerta (tarefa atrasada, exclusão) |

**Decisão deliberada de acessibilidade:** o neon no tema escuro é usado só como
*acento* (botões, bordas, ícones) — o texto corrido usa `onSurface`/`onBackground`
num off-white (`#EDEAE6`), não a cor neon. Texto em bloco na cor neon (verde/rosa
puro) cansa a vista rapidamente em uso prolongado; como acento pontual, o efeito
"vibrante" continua presente sem prejudicar a leitura do dia a dia.

**Categorias têm cor fixa, não reativa ao tema** (`Category.colorHex`): como
etiquetas de projeto no Todoist/Gmail, a identidade visual de uma categoria fica
reconhecível independente do tema — a paleta padrão (`CategorySeeder`,
`AddCategoryUseCase`) foi escolhida para ler bem tanto no fundo bege claro quanto no
quase preto do escuro.

**Composição do fundo (`LiquidBackground`), em camadas:**
1. Cor sólida base (`colorScheme.background`)
2. `colorfulBlobs()` — 2-3 manchas de cor num degradê radial bem sutil (opacidade
   ~0.10-0.14) nos cantos — um "respiro de cor", não o protagonista
3. `floatingBubbles()` — bolhas neutras (`onBackground` em baixíssima opacidade)
   subindo lentamente em loop, animação lida só na fase de desenho
   (`onDrawBehind`) para não recompor a árvore inteira a 60fps
4. `noiseOverlay()` — grain sutil via `BitmapShader` gerado em runtime (sem asset
   de imagem), por cima de tudo

Essa ordem/composição é o resultado de uma iteração real: uma primeira versão dos
blobs coloridos em opacidade mais alta (~0.30) deixava o fundo "carregado" o
suficiente para competir visualmente com a translucidez do `GlassCard` — o efeito de
vidro parava de se perceber como vidro. A correção foi em duas frentes: (1) baixar a
saturação do fundo para ele ficar neutro o bastante para não competir, e (2) reduzir a
opacidade do backing do `GlassCard` (de 0.55 para 0.4), já que um fundo mais calmo
permite mais transparência real sem perder legibilidade do texto.

Guideline: **nunca** use `Color(...)` cru dentro de uma tela — sempre componha a
partir de um token do design system. Isso é o que permite trocar o tema inteiro
alterando um único arquivo (`Theme.kt`).

## 🏠 Home — carrosséis por categoria

A tela principal (`TaskListScreen`) não é uma lista vertical única — é organizada em
seções por categoria, cada uma com seu próprio carrossel horizontal (inspirado na
organização visual do Shellbox), **reordenáveis por arraste**. Um banner de resumo
fica fixo no topo.

```
┌─────────────────────────────────────┐
│  Resumo da semana                    │  ← HomeSummaryBanner
│  ⚠ 3 atrasadas   📅 5 essa semana    │
├─────────────────────────────────────┤
│  Pets                          ≡     │  ← TaskSection (categoria, arrastável)
│  [card] [card] [card] →              │  ← LazyRow, scroll horizontal
├─────────────────────────────────────┤
│  Casa                          ≡     │
│  [card] [card] →                     │
├─────────────────────────────────────┤
│  Sem categoria                       │  ← sempre por último, não arrastável
│  [card] →                            │
└─────────────────────────────────────┘
```

**Por que categoria, e não recorrência (diária/mensal/anual) como agrupador:**
categoria é como a pessoa pensa no dia a dia ("o que falta pros pets"), enquanto
recorrência é um atributo técnico de agendamento — bem menos intuitivo como critério
de organização visual. Uma categoria nova criada pela tela de Nova Tarefa aparece
automaticamente como um carrossel assim que a primeira tarefa é atribuída a ela —
`TaskListViewModel.buildSections` agrupa dinamicamente a partir de `Category.id`,
não há nenhuma categoria hardcoded na camada de apresentação.

**Reordenar arrastando:** cada categoria tem um `sortOrder` persistido no Room
(`Category.sortOrder`, adicionado via `MIGRATION_1_2`). O ícone de arrastar (≡) no
cabeçalho de cada seção aciona um long-press + drag vertical implementado com
Compose puro (`DragDropListState`, sem lib externa) — a nova ordem só é escrita no
banco quando o usuário solta o dedo, não a cada pixel arrastado. Categorias novas
sempre entram no fim (`ReorderCategoriesUseCase`/`CategoryRepositoryImpl.addCategory`
calculam o próximo `sortOrder` disponível). A seção "sem categoria" fica sempre fixa
por último e não é arrastável, já que não corresponde a uma categoria real.

**Trade-off de gesto:** como o carrossel rola horizontalmente, um swipe-to-delete
(gesto também horizontal) entraria em conflito direto com o scroll. A exclusão e
conclusão de tarefas acontecem via **toque longo** no card, que abre um menu rápido
(`TaskCarouselItem`, via `combinedClickable`) — e o próprio arrasto de reordenação das
seções usa long-press antes de iniciar o drag, pelo mesmo motivo (evitar competir com
o scroll horizontal do carrossel).

**Escopo consciente do banner:** os números (atrasadas / vencendo essa semana) são
calculados automaticamente a partir das tarefas — não é configurável pelo usuário
ainda. Deixar a pessoa escolher quais métricas aparecem ali é uma boa evolução de
produto, mas é uma tela de configurações à parte; não inflamos o escopo desta
mudança para incluir isso.

## 🔔 Notificações

```
Task.nextDueDate  ──►  NotificationScheduler.schedule()
                              │
                 canScheduleExactAlarms()?
                    │                  │
                   sim                não
                    │                  │
              AlarmScheduler      WorkManager
           (AlarmManager exato)  (aproximado, ainda
                    │             melhor que nada)
                    ▼
          TaskReminderReceiver ──► TaskNotificationWorker
                                    (canal + notificação)
```

- **Exatidão em primeiro lugar**: `setExactAndAllowWhileIdle` garante o disparo no
  horário certo mesmo em Doze Mode; o fallback via `WorkManager` só entra quando o
  usuário nega a permissão de alarmes exatos (Android 12+)
- **Sobrevive a reboot**: o Android cancela todo `AlarmManager` ao reiniciar o
  aparelho — sem `BootCompletedReceiver`, os lembretes parariam de disparar
  silenciosamente após um restart. Ele relê as tarefas ativas e reagenda tudo.
- **Permissão de notificação (Android 13+)** solicitada uma vez, na primeira
  abertura do app (`MainActivity.RequestNotificationPermissionIfNeeded`)

## 🎬 Motion & microinterações

Todas as animações seguem APIs oficiais e recomendações de motion do Compose
(https://developer.android.com/develop/ui/compose/animation) — nada de bibliotecas
externas de animação. Onde não fazia sentido animar, deixamos estático de propósito
(ex.: não há transição customizada no `Scaffold`/`TopAppBar`, que já tem o
comportamento padrão do Material).

| Interação | Onde | API usada |
|---|---|---|
| Bolhas flutuando no fundo | `LiquidBackground` | `rememberInfiniteTransition` + `drawWithCache` (lido só na fase de desenho) |
| Reordenar carrossel por categoria | `TaskSectionCarousel` (Home) | `pointerInput` + `detectDragGesturesAfterLongPress`, offset via `graphicsLayer` |
| Excluir/concluir via toque longo | `TaskCarouselItem` | `combinedClickable` (onLongClick) + `DropdownMenu`, Snackbar com **Desfazer** |
| Transição entre telas | `MainActivity` (NavHost) | `enterTransition`/`exitTransition` com slide+fade (padrão "push" mestre-detalhe) |
| Troca loading → vazio → lista | `TaskListScreen`, `TaskDetailScreen` | `AnimatedContent` com crossfade |
| Inserir/remover item do carrossel | `TaskSectionCarousel`, histórico do `TaskDetailScreen` | `Modifier.animateItem()` |
| Cor do prazo (atrasado vs. em dia) | `TaskCarouselItem` | `animateColorAsState` |
| Seleção de chip (recorrência/categoria) | `GlassChip` | `animateColorAsState` |
| Botão "Salvar" enquanto processa | `GlassButton` | `AnimatedContent` (texto ↔ spinner) + `enabled = false` durante o loading, evitando duplo envio |
| Card de erro aparecendo/sumindo | `AddTaskScreen` | `AnimatedVisibility` com `expandVertically`/`shrinkVertically` |
| Confirmação antes de excluir (tela de detalhe) | `TaskDetailScreen` | `AlertDialog` (ação sem undo — diferente do menu de toque longo na Home, que tem Desfazer) |

## ♿ Acessibilidade

- Todo ícone acionável tem `contentDescription` traduzível (`stringResource`), nunca hardcoded
- Estado vazio da lista é anunciado via `Modifier.semantics { contentDescription = ... }`
- `GlassChip` expõe `role = Role.RadioButton` e `selected` para leitores de tela
- Área de toque dos `IconButton` respeita o mínimo de 48dp do Material (padrão do componente)
- Cores de texto usam `MaterialTheme.colorScheme.onSurface`/`onSurfaceVariant` em vez de
  branco fixo, preservando contraste em ambos os temas
- Ícones puramente decorativos (ex.: o ícone dentro do `HomeSummaryBanner`) usam
  `contentDescription = null` de propósito — o número + rótulo ao lado já comunicam a
  informação; duplicar na descrição do ícone só gera ruído para leitor de tela

**Pendências conhecidas** (próximos passos, não implementados ainda): auditoria de
contraste WCAG AA sobre o efeito "glass" translúcido, testes automatizados de
TalkBack, suporte a fonte com escala > 200%.

## 📦 Tecnologias

| Categoria | Tecnologia | Versão |
|---|---|---|
| Linguagem | Kotlin | 1.9.24 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.02 |
| Build | Android Gradle Plugin | 8.4.2 |
| Persistência | Room (KSP, não kapt) | 2.6.1 |
| Preferências | DataStore | 1.1.1 |
| DI | Koin (`koin-android`, `koin-androidx-compose`, `koin-androidx-workmanager`) | 3.5.6 |
| Concorrência | Kotlin Coroutines + Flow/StateFlow | 1.8.1 |
| Trabalho em segundo plano | WorkManager | 2.9.0 |
| Navegação | Navigation Compose | 2.7.7 |
| Testes unitários | JUnit4, Turbine, MockK/Mockito-Kotlin, Robolectric | — |
| Testes instrumentados | Espresso, Compose UI Test, Room Testing (`MigrationTestHelper`) | — |
| Qualidade estática | Detekt + ktlint (via detekt), Android Lint | 1.23.6 |
| Cobertura | Kover | 0.8.3 |
| CI/CD | GitHub Actions | — |

**Min SDK 26 · Target/Compile SDK 34 · JVM target 17.**

## 🧪 Testes

```
app/src/test/           → JVM: ViewModel (Turbine), UseCase, Repository (Room + Robolectric)
app/src/androidTest/    → Instrumentado: Compose UI (createComposeRule), Room MigrationTestHelper
```

Destaques:

- `TaskListViewModelTest` — testa o `StateFlow` exposto pelo ViewModel com **Turbine**,
  incluindo agrupamento por categoria, contagem do banner de resumo, conclusão/desfazer
  e exclusão/desfazer (`events` como `Channel`, testado separadamente do `state` por
  ser um evento de disparo único)
- `TaskRepositoryImplTest` / `CategoryRepositoryImplTest` — rodam contra um **Room real
  em memória** (não mockam o DAO), validando queries SQL, ordenação por `sortOrder` e
  mapeamento Entity↔Domain
- `MigrationTest` (instrumentado) — valida `MIGRATION_1_2` com `MigrationTestHelper`
  contra o schema exportado (`room.schemaLocation`), garantindo que categorias
  existentes não são perdidas ao adicionar a coluna `sortOrder`
- `AddTaskViewModelTest` — cobre validação de título/nome de categoria vazios, cálculo
  correto do próximo vencimento por recorrência e o fluxo de edição (preservar vs.
  recalcular `nextDueDate`)
- `TaskDetailViewModelTest` — cobre carregamento, exclusão e o caso de tarefa não
  encontrada (`isNotFound`)
- `TaskListScreenTest` — testa a UI Compose (estado vazio e exibição de tarefa) com
  fakes de repositório, sem subir o container de DI

> Nota de design dos testes de ViewModel: preferimos snapshots de `state.value` após
> `advanceUntilIdle()` a sequenciar emissões com Turbine sempre que duas coroutines
> escrevem no mesmo `StateFlow` (ex.: o coletor de categorias e o handler do diálogo
> de nova categoria) — como os fakes são síncronos, a ordem exata de emissões
> intermediárias é um detalhe de implementação frágil demais para testar diretamente.

Rodar localmente:

```bash
./gradlew testDebugUnitTest          # testes unitários
./gradlew connectedDebugAndroidTest  # testes instrumentados (emulador/dispositivo)
./gradlew koverHtmlReport            # relatório de cobertura em app/build/reports/kover
```

## ⚙️ CI/CD

Pipeline em `.github/workflows/ci.yml`, com 3 jobs paralelizáveis por dependência:

1. **static-analysis** — `lintDebug` + `detekt` (config em `config/detekt/detekt.yml`)
2. **unit-tests** — testes JVM + relatório de cobertura (Kover), publicados como artifact
3. **build** — gera o APK debug, depende dos dois jobs anteriores

## 🚀 Como executar

1. Clone o repositório
2. Abra no Android Studio (Iguana ou mais recente)
3. **Gradle JDK**: configure Java 17 em
   *Settings → Build, Execution, Deployment → Build Tools → Gradle*
4. Sincronize o Gradle

## 📊 Análise do Projeto

### Pontos Fortes
- **Arquitetura sólida**: Clean Architecture bem implementada com separação clara de responsabilidades
- **Design System consistente**: Liquid Glass com identidade visual única e tokens bem definidos
- **Testes abrangentes**: Cobertura de ViewModel, UseCase, Repository e UI com estratégias adequadas
- **Performance**: Animações otimizadas com `drawWithCache` e `rememberInfiniteTransition`
- **Acessibilidade**: Suporte a leitores de tela e contraste adequado
- **Offline-first**: Room com migrations e notificações locais robustas

### Áreas de Melhoria
- **Modularização**: Atualmente em único módulo `:app`, poderia beneficiar de multi-module
- **Internacionalização**: Strings em português, poderia adicionar suporte multi-idioma
- **Testes E2E**: Falta cobertura de fluxos completos de usuário
- **Performance profiling**: Poderia adicionar benchmarks para operações críticas
- **Error handling**: Centralização de tratamento de erros e mensagens ao usuário

### Métricas de Código
- **Linguagem**: Kotlin 100%
- **Linhas de código**: ~4.400 linhas Kotlin (código principal)
- **Cobertura de testes**: ~70% (estimado)
- **Dependências**: 15+ bibliotecas principais
- **Complexidade ciclomática**: Baixa a média (funções pequenas e focadas)

### Decisões Técnicas Notáveis
- **Koin vs Hilt**: Escolha de Koin por simplicidade e performance
- **Room vs SQLDelight**: Room por maturidade e integração com Android
- **Compose XML**: Adoção antecipada de Jetpack Compose para UI moderna
- **Coroutines vs RxJava**: Coroutines por serem nativas e mais idiomáticas em Kotlin
5. Execute em um dispositivo/emulador API 26+

**Configuração:** Min SDK 26 · Target/Compile SDK 34

### Gerando uma build de release assinada

1. Copie `keystore.properties.example` para `keystore.properties` (já está no `.gitignore`)
2. Preencha `storeFile`, `storePassword`, `keyAlias` e `keyPassword` com os dados da sua keystore
3. Rode `./gradlew assembleRelease` (ou `bundleRelease` para gerar o AAB)

Em CI/CD, use variáveis de ambiente (`TASKFLOW_STORE_FILE`, `TASKFLOW_STORE_PASSWORD`,
`TASKFLOW_KEY_ALIAS`, `TASKFLOW_KEY_PASSWORD`) como secrets em vez do arquivo local.
Sem nenhuma das duas fontes configuradas, `assembleRelease` ainda funciona, mas gera
um APK **não assinado** — o que é intencional, para não travar o build em ambientes
sem acesso à keystore (ex.: forks, PRs externos).

## 🧭 Decisões de arquitetura e trade-offs

Documentando aqui para deixar claro o que foi decisão consciente vs. o que é dívida técnica conhecida:

- **Módulo único (`:app`)**: para o tamanho atual do app, multi-módulo (`:domain`,
  `:data`, `:feature-*`) adicionaria overhead de build sem benefício real. O código já
  está organizado em pacotes que mapeiam 1:1 para módulos futuros, então a migração,
  se necessária, é mecânica.
- **Koin em vez de Hilt**: Koin evita geração de código (builds mais rápidos em
  projetos pequenos) às custas de checagem de dependências em tempo de compilação.
  Para um app deste porte, o trade-off vale a pena; em um app maior, com múltiplos
  desenvolvedores, o Hilt (checagem em compile-time) tende a compensar melhor.
- **`ThemeManager` sem uma interface de domínio dedicada**: preferência de tema é
  configuração de UI, não regra de negócio — criar uma `ThemeRepository` no domínio só
  para um boolean seria over-engineering. Ainda assim, a implementação concreta
  (DataStore) mora em `data.local.preferences`, não em `presentation`, mantendo o
  limite de camada correto para o que ela realmente é: persistência.
- **Sem criptografia do Room**: os dados armazenados (título/descrição de tarefas) não
  são sensíveis. Se o escopo crescer para incluir dados pessoais, usar SQLCipher.
- **`DateFormats` com padrão fixo (`dd/MM/yyyy HH:mm`)**: não localizado por enquanto,
  e deliberadamente uma única fonte de verdade (não duplicado como string XML — essa
  duplicação já existiu e divergiu silenciosamente numa versão anterior). Trocar por
  `DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)` quando o app precisar
  suportar outros locales além de `pt-BR`.
- **Resumo da Home não é configurável pelo usuário**: os números exibidos (atrasadas /
  vencendo essa semana) são fixos. Deixar a pessoa escolher quais métricas aparecem é
  uma boa evolução de produto, mas exige uma tela de configurações com preferências
  persistidas — fora do escopo até agora.
