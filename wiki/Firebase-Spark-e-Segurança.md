# Firebase Spark e segurança

## Serviços utilizados

- Firebase Authentication para contas por e-mail e senha.
- Cloud Firestore para listas, itens, participantes e convites.
- Firebase Security Rules para autorização e validação.

O aplicativo não usa Cloud Functions. Todas as operações são realizadas diretamente no Firestore por transações ou lotes atômicos protegidos pelas regras.

## Plano gratuito

O ambiente foi desenhado para o plano Spark, sem faturamento vinculado. Se uma cota gratuita for atingida, a operação pode ficar temporariamente indisponível, mas não gera cobrança automática.

## Controles de acesso

- Todas as operações são negadas por padrão.
- Somente participantes podem ler uma lista e seus itens.
- Somente o proprietário pode administrar o ciclo de vida da lista.
- Itens só podem ser alterados enquanto a lista está ativa, exceto pela reinicialização atômica durante a reutilização.
- Convites não podem ser listados; a leitura exige conhecer o código secreto.
- Saída e transferência de propriedade exigem atualizações consistentes da lista e dos documentos de participação.

## Privacidade

Compartilhe códigos de convite somente com pessoas autorizadas. Não publique `google-services.json`, credenciais, arquivos `.env`, chaves ou configurações locais no repositório.

## Limitações

O plano Spark possui cotas de leitura, escrita, armazenamento e autenticação. Para um aplicativo pequeno e familiar, elas tendem a ser suficientes, mas devem ser monitoradas no Console Firebase.
