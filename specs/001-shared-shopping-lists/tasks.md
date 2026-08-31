# Tarefas: Listas de Compras Compartilhadas

**Entrada**: Documentos de design em specs/001-shared-shopping-lists/  
**Pré-requisitos**: plan.md, spec.md, research.md, data-model.md, contracts/firestore-contract.md e quickstart.md  
**Testes**: Não foram solicitados como abordagem TDD na especificação; a validação ponta a ponta seguirá quickstart.md e o Emulator Suite.

## Formato

- **[P]**: pode ser executada em paralelo, pois atua em arquivos distintos e não depende de tarefa incompleta.
- **[USn]**: história de usuário à qual a tarefa pertence.

## Fase 1: Preparação

**Objetivo**: Criar a estrutura do aplicativo Android e a configuração local do Firebase.

- [X] T001 Criar o projeto Gradle Android com módulos de aplicativo em android/settings.gradle.kts e android/app/build.gradle.kts
- [X] T002 Configurar Kotlin, Jetpack Compose, Material 3, Navigation, Hilt, Firebase Authentication, Firestore e Functions em android/app/build.gradle.kts
- [X] T003 [P] Criar a estrutura de pacotes core, data e feature em android/app/src/main/java/
- [X] T004 [P] Criar o projeto de regras e emuladores Firebase em firebase/firebase.json, firebase/.firebaserc e firebase/firestore.indexes.json
- [X] T005 [P] Criar o esqueleto de Cloud Functions em firebase/functions/package.json e firebase/functions/src/index.ts

---

## Fase 2: Base Compartilhada

**Objetivo**: Implementar as peças que bloqueiam todos os fluxos de usuário.

- [X] T006 Criar a configuração de injeção Hilt e o container Firebase em android/app/src/main/java/com/collaborativeshoppinglist/core/di/FirebaseModule.kt
- [X] T007 [P] Criar modelos de domínio de usuário, lista, item, participante e convite em android/app/src/main/java/com/collaborativeshoppinglist/data/model/
- [X] T008 [P] Criar normalização de nomes de itens e validações de nome/quantidade em android/app/src/main/java/com/collaborativeshoppinglist/core/validation/ItemValidator.kt
- [X] T009 Criar mapeamento de erros Firebase para erros de domínio exibíveis em android/app/src/main/java/com/collaborativeshoppinglist/core/error/AppErrorMapper.kt
- [X] T010 Criar rotas de navegação e estrutura de tela autenticada em android/app/src/main/java/com/collaborativeshoppinglist/core/navigation/AppNavigation.kt
- [X] T011 Criar Firebase Security Rules com negação padrão e leitura limitada a participantes em firebase/firestore.rules
- [X] T012 Configurar o Firebase Local Emulator Suite e variáveis locais de desenvolvimento em firebase/firebase.json e android/local.properties.example

**Ponto de controle**: a estrutura, as regras-base e a navegação estão prontas; as histórias podem começar.

---

## Fase 3: História de Usuário 1 — Gerenciar uma lista pessoal (Prioridade: P1) 🎯 MVP

**Objetivo**: Permitir criar conta, entrar, criar uma lista e gerenciar seus itens.

**Teste independente**: Uma pessoa cria uma conta, entra, cria uma lista, adiciona um item, altera a quantidade, remove-o e verifica o resultado.

- [X] T013 [US1] Implementar registro, entrada e saída por e-mail e senha em android/app/src/main/java/com/collaborativeshoppinglist/data/repository/AuthRepository.kt
- [X] T014 [US1] Criar ViewModel e telas de registro/entrada em android/app/src/main/java/com/collaborativeshoppinglist/feature/auth/
- [X] T015 [US1] Implementar criação e consulta de listas próprias em android/app/src/main/java/com/collaborativeshoppinglist/data/repository/ShoppingListRepository.kt
- [X] T016 [US1] Criar tela de lista de compras e formulário de criação de lista em android/app/src/main/java/com/collaborativeshoppinglist/feature/lists/ListOverviewScreen.kt
- [X] T017 [US1] Implementar transações de criação, incremento por nome normalizado, alteração de quantidade e remoção de itens em android/app/src/main/java/com/collaborativeshoppinglist/data/repository/ShoppingListRepository.kt
- [X] T018 [US1] Criar ViewModel de detalhes da lista com estado de carregamento, erro e nova tentativa em android/app/src/main/java/com/collaborativeshoppinglist/feature/lists/ListDetailViewModel.kt
- [X] T019 [US1] Criar a interface de itens com adicionar, editar quantidade, remover e mensagens de validação em android/app/src/main/java/com/collaborativeshoppinglist/feature/lists/ListDetailScreen.kt
- [X] T020 [US1] Conectar listeners do Firestore para listas e itens em android/app/src/main/java/com/collaborativeshoppinglist/data/source/FirestoreShoppingListDataSource.kt

**Ponto de controle**: lista individual funcional, com quantidade mínima 1 e união de nomes equivalentes.

---

## Fase 4: História de Usuário 2 — Acompanhar itens durante uma compra (Prioridade: P1)

**Objetivo**: Permitir marcar e desmarcar itens e mostrar quem realizou a marcação.

**Teste independente**: Um participante marca e desmarca um item, e o status e o responsável mudam corretamente.

- [X] T021 [US2] Implementar transação para marcar e desmarcar item com autor e horário do servidor em android/app/src/main/java/com/collaborativeshoppinglist/data/repository/ShoppingListRepository.kt
- [X] T022 [US2] Adicionar ação de marcar/desmarcar e estado visual pendente/no carrinho em android/app/src/main/java/com/collaborativeshoppinglist/feature/lists/ListItemRow.kt
- [X] T023 [US2] Exibir o participante que marcou o item e tratar atualização em tempo real em android/app/src/main/java/com/collaborativeshoppinglist/feature/lists/ListDetailScreen.kt

**Ponto de controle**: o progresso de compra e sua autoria são sincronizados para participantes online.

---

## Fase 5: História de Usuário 3 — Compartilhar e participar de uma lista (Prioridade: P1)

**Objetivo**: Permitir convidar usuários registrados, aceitar convites e visualizar participantes.

**Teste independente**: O proprietário convida uma segunda conta, a segunda aceita e ambas visualizam a mesma lista e seus participantes.

- [X] T024 [US3] Implementar Cloud Function autenticada para criar convite com expiração de 3 horas em firebase/functions/src/createInvitation.ts
- [X] T025 [US3] Implementar Cloud Function transacional para aceitar convite, criar participação e validar expiração/lista ativa em firebase/functions/src/acceptInvitation.ts
- [X] T026 [US3] Integrar chamadas de convite e aceite no repositório em android/app/src/main/java/com/collaborativeshoppinglist/data/repository/InvitationRepository.kt
- [X] T027 [US3] Criar tela de envio de convite para proprietário em android/app/src/main/java/com/collaborativeshoppinglist/feature/invitations/CreateInvitationScreen.kt
- [X] T028 [US3] Criar lista de convites pendentes e fluxo de aceite em android/app/src/main/java/com/collaborativeshoppinglist/feature/invitations/InvitationInboxScreen.kt
- [X] T029 [US3] Exibir participantes e atualizar o acesso da lista aceita em android/app/src/main/java/com/collaborativeshoppinglist/feature/lists/ParticipantsSection.kt
- [X] T030 [US3] Estender as permissões de proprietário, participante, convite e aceite em firebase/firestore.rules

**Ponto de controle**: usuários autorizados podem entrar na mesma lista e as alterações tornam-se compartilhadas em tempo real.

---

## Fase 6: História de Usuário 4 — Encerrar uma lista concluída (Prioridade: P2)

**Objetivo**: Permitir que somente o proprietário encerre uma lista e bloqueie alterações posteriores.

**Teste independente**: O proprietário encerra uma lista e todos os participantes a veem como somente leitura.

- [X] T031 [US4] Implementar transação de encerramento e invalidação de convites pendentes em firebase/functions/src/closeShoppingList.ts
- [X] T032 [US4] Integrar o encerramento no repositório e mapear o erro LIST_CLOSED em android/app/src/main/java/com/collaborativeshoppinglist/data/repository/ShoppingListRepository.kt
- [X] T033 [US4] Criar ação de encerrar para proprietário e estado somente leitura para lista encerrada em android/app/src/main/java/com/collaborativeshoppinglist/feature/lists/ListDetailScreen.kt
- [X] T034 [US4] Restringir alterações e convites em listas CLOSED em firebase/firestore.rules

**Ponto de controle**: uma lista encerrada permanece visível, mas não pode mais ser modificada nem receber convites.

---

## Fase 7: Acabamento e Validação Transversal

**Objetivo**: Garantir qualidade de uso, segurança e aderência aos cenários definidos.

- [X] T035 [P] Revisar acessibilidade, textos de erro, estados vazios e carregamento em android/app/src/main/java/com/collaborativeshoppinglist/feature/
- [X] T036 [P] Adicionar observabilidade mínima para falhas de transação e Functions em firebase/functions/src/ e android/app/src/main/java/com/collaborativeshoppinglist/core/
- [X] T037 Validar manualmente os quatro cenários de quickstart.md usando duas contas e o Firebase Local Emulator Suite
- [X] T038 Validar autorização, expiração de convite, lista encerrada e concorrência contra firebase/firestore.rules no Firebase Local Emulator Suite
- [X] T039 Atualizar o guia de execução do projeto em README.md

---

## Dependências e Ordem de Execução

1. Fase 1 → Fase 2.
2. A História 1 é a primeira entrega mínima utilizável e depende da Fase 2.
3. A História 2 depende da base de itens da História 1.
4. A História 3 depende de autenticação e listas da História 1, mas seu código de Functions pode iniciar em paralelo com a interface da História 2.
5. A História 4 depende do modelo de lista e da infraestrutura de Functions da História 3.
6. A Fase 7 depende das histórias que se deseja entregar.

## Oportunidades de Paralelismo

- T003, T004 e T005 podem ocorrer em paralelo após T001.
- T007, T008 e T011 podem ocorrer em paralelo depois de T002.
- Na História 1, T013 e T015 podem avançar em paralelo após a base compartilhada.
- Após a História 1, T021 e T024 podem ser realizadas em paralelo: acompanham itens e constroem o backend de convites em arquivos distintos.
- Na História 3, T027 e T028 podem ser realizadas em paralelo após T026.
- T035 e T036 podem ser realizadas em paralelo.

## Estratégia de Implementação

### MVP primeiro

1. Concluir as Fases 1 e 2.
2. Concluir a História 1.
3. Validar o cenário de lista individual em quickstart.md antes de continuar.

### Entrega incremental

1. Adicionar a História 2 para acompanhar o carrinho.
2. Adicionar a História 3 para compartilhar listas.
3. Adicionar a História 4 para finalizar compras.
4. Executar a Fase 7 antes da entrega.

Todas as tarefas seguem o formato obrigatório de checklist, possuem identificador, caminho e rótulo de história quando aplicável.

## Fase 8: Refatoração para Firebase Spark

**Objetivo**: Eliminar serviços faturáveis e preservar compartilhamento seguro usando somente Authentication, Firestore e Security Rules no plano Spark.

- [X] T040 [US3] Atualizar especificação, pesquisa, modelo e contrato para convites por código em specs/001-shared-shopping-lists/
- [X] T041 [US3] Substituir criação e aceite via Functions por transações diretas em android/app/src/main/java/com/collaborativeshoppinglist/data/repository/InvitationRepository.kt
- [X] T042 [US3] Adaptar telas e estado de convites para geração e entrada de código em android/app/src/main/java/com/collaborativeshoppinglist/feature/invitations/
- [X] T043 [US4] Mover encerramento da lista para transação direta em android/app/src/main/java/com/collaborativeshoppinglist/data/repository/ShoppingListRepository.kt
- [X] T044 Reforçar criação, aceite de uso único, expiração e encerramento em firebase/firestore.rules
- [X] T045 Remover Cloud Functions das dependências e configurações em android/app/build.gradle.kts e firebase/firebase.json
- [X] T046 Atualizar testes do Emulator Suite para o fluxo Spark em firebase/functions/test/emulator-e2e.mjs
- [X] T047 Atualizar instruções de execução e migração ao plano Spark em README.md
- [X] T048 Validar build Android e testes de regras sem emulador de Functions
