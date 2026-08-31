# Pesquisa Técnica: Listas de Compras Compartilhadas

## Arquitetura Android

**Decisão**: Kotlin, Jetpack Compose, Material 3, ViewModel, StateFlow, fluxo unidirecional de dados e Hilt.

**Justificativa**: São recomendações da plataforma Android e permitem telas reativas, testáveis e organizadas por funcionalidade sem introduzir módulos ou camadas excessivas no MVP.

**Alternativas consideradas**: Views XML e arquitetura em múltiplos módulos. Foram rejeitadas porque aumentam a quantidade de código e a complexidade inicial sem atender uma necessidade do MVP.

## Dados, autenticação e tempo real

**Decisão**: Firebase Authentication por e-mail e senha, Cloud Firestore e Cloud Functions para criação e aceite de convites.

**Justificativa**: O Firestore fornece listeners em tempo real, transações atômicas e SDK Android. Authentication e Security Rules permitem limitar dados aos participantes; Functions protegem operações de convite que envolvem múltiplos documentos e expiração.

**Alternativas consideradas**: Supabase e backend próprio. Ambos são viáveis, mas exigem mais infraestrutura e configuração de autenticação, persistência e sincronização para o MVP.

## Concorrência e conectividade

**Decisão**: Todas as mutações de itens, aceite de convite e fechamento de lista usam transações do Firestore. A última transação confirmada pelo servidor define o estado final. A interface só confirma uma ação após êxito da transação.

**Justificativa**: Transações são repetidas automaticamente quando há concorrência e só confirmam mudanças atômicas. Isso mantém a regra de “última alteração confirmada vence” e impede que uma ação offline pareça concluída quando não foi.

**Alternativas consideradas**: Escritas não transacionais com sincronização offline. Foram rejeitadas para comandos críticos porque o comportamento de última gravação ao reconectar não atende ao requisito de informar falha e permitir nova tentativa.

## Segurança

**Decisão**: Security Rules começam com negação por padrão. Acesso a uma lista exige participação; somente o proprietário pode fechá-la e criar convites; somente participantes podem alterar itens de listas ativas.

**Justificativa**: A autorização precisa ser aplicada fora do cliente. Rules serão validadas no Emulator Suite antes da entrega.

**Alternativas consideradas**: Controle apenas na interface Android. Rejeitado porque pode ser contornado.

## Fontes

- [Recomendações de arquitetura Android](https://developer.android.com/topic/architecture/recommendations)
- [Arquitetura com Compose](https://developer.android.com/develop/ui/compose/architecture)
- [Hilt no Android](https://developer.android.com/training/dependency-injection/hilt-android)
- [Transações do Firestore](https://firebase.google.com/docs/firestore/manage-data/transactions)
- [Listeners do Firestore](https://firebase.google.com/docs/firestore/query-data/listen)
- [Regras de segurança do Firestore](https://firebase.google.com/docs/firestore/security/get-started)
- [Firebase Emulator Suite para regras](https://firebase.google.com/docs/rules)
