# Plano de Implementação: Listas de Compras Compartilhadas

**Branch**: `001-shared-shopping-lists` | **Data**: 2026-08-29 | **Especificação**: [spec.md](./spec.md)

**Entrada**: Especificação consolidada em `specs/001-shared-shopping-lists/spec.md`.

## Resumo

Implementar um aplicativo Android de listas de compras compartilhadas, com autenticação, convites, edição de itens e sincronização em tempo real. O aplicativo será desenvolvido em Kotlin com Jetpack Compose; Firebase Authentication, Cloud Firestore e Cloud Functions fornecerão autenticação, dados, autorização e operações sensíveis de convite. O Firestore será a fonte de verdade para atualizações em tempo real.

## Contexto Técnico

**Linguagem/Versão**: Kotlin estável, com JDK 17 e SDK Android atual compatível.

**Dependências Principais**: Jetpack Compose e Material 3, Navigation Compose, ViewModel, StateFlow, Hilt, Firebase Authentication, Cloud Firestore e Cloud Functions for Firebase.

**Armazenamento**: Cloud Firestore; o cache local do SDK é somente suporte de leitura, não autoridade.

**Testes**: Testes unitários com JUnit e fakes, testes de interface/instrumentação do Compose e testes de integração de Auth, Firestore e Security Rules com Firebase Local Emulator Suite.

**Plataforma-alvo**: Android.

**Tipo de Projeto**: Aplicativo móvel Android com serviços Firebase gerenciados.

**Metas de Desempenho**: 95% das alterações visíveis para participantes online em até 3 segundos; interface responsiva a 60 fps nas telas principais.

**Restrições**: Controle de acesso obrigatório no servidor por Firebase Security Rules; nenhuma credencial de servidor no aplicativo; expiração de convite calculada por horário do servidor; alterações transacionais não são confirmadas ao usuário antes de confirmação do servidor.

**Escala/Escopo**: MVP com autenticação por e-mail e senha, listas colaborativas, itens, convites e fechamento de lista; sem pagamentos, catálogo, preços, IA ou notificações push.

## Verificação da Constituição

A constituição atual é um modelo sem princípios ratificados. Não há regras de governança aplicáveis que bloqueiem este plano. O plano adota, como controles mínimos, testes automatizados para regras críticas, autorização por padrão negada e validação das regras de segurança no emulador.

**Resultado antes da pesquisa**: aprovado.  
**Resultado após o design**: aprovado; não há violações a justificar.

## Estrutura do Projeto

### Documentação desta funcionalidade

```text
specs/001-shared-shopping-lists/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── firestore-contract.md
└── tasks.md             # gerado posteriormente
```

### Código-fonte (raiz do repositório)

```text
android/
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/.../
│       │   │   ├── core/
│       │   │   ├── data/
│       │   │   └── feature/
│       │   │       ├── auth/
│       │   │       ├── lists/
│       │   │       └── invitations/
│       │   └── androidTest/
│       └── test/
└── build.gradle.kts

firebase/
├── firestore.rules
├── firestore.indexes.json
└── functions/
    └── src/
```

**Decisão de estrutura**: Um único aplicativo Android organizado por funcionalidade mantém o MVP simples. O código comum fica em `core/` e `data/`; cada fluxo de usuário fica em `feature/`. Funções e regras Firebase permanecem separadas em `firebase/`, pois são implantadas e testadas com o Firebase Emulator Suite.

## Registro de Complexidade

Nenhuma violação de constituição requer justificativa.

| Violação | Por que é necessária | Alternativa mais simples rejeitada porque |
|-----------|---------------------|-------------------------------------------|
| Nenhuma | — | — |
