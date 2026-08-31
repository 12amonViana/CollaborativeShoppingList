# Contrato de Dados e Acesso do Firestore

## Operações do Aplicativo

| Operação | Autor | Precondições | Resultado |
|---|---|---|---|
| Criar lista | Usuário autenticado | Nome válido | Cria lista ativa e participação OWNER |
| Adicionar item | Participante | Lista ativa; nome válido | Cria item com quantidade 1 ou soma 1 a item equivalente |
| Alterar quantidade | Participante | Lista ativa; quantidade inteira >= 1 | Atualiza a quantidade |
| Marcar/desmarcar | Participante | Lista ativa | Atualiza inCart, autoria e horário |
| Remover item | Participante | Lista ativa; item existe | Exclui o item |
| Criar convite | Proprietário | Lista ativa | Transação cria código aleatório pendente de 3 horas |
| Aceitar convite | Usuário autenticado com o código | Convite pendente, não expirado; lista ativa | Transação cria participação, atualiza a lista e aceita convite |
| Encerrar lista | Proprietário | Lista ativa | Transação fecha a lista; convites pendentes tornam-se inutilizáveis |

Todas as operações de alteração são transacionais. Em alterações concorrentes do mesmo campo, a última transação confirmada pelo servidor vence.

## Eventos em Tempo Real

O aplicativo mantém listeners apenas para listas das quais o usuário é participante, seus itens, membros e os próprios convites. A interface trata cada alteração confirmada do Firestore como estado canônico e atualiza a tela em até 3 segundos para participantes online.

## Firebase Security Rules — Política

- Todas as leituras e escritas são negadas por padrão.
- Um usuário só pode ler uma lista, seus itens e membros quando possui participação.
- Apenas o proprietário pode encerrar lista e iniciar convite.
- Participantes podem alterar itens somente enquanto a lista estiver ACTIVE.
- Convites não podem ser listados; um usuário autenticado só pode buscar um documento quando já conhece seu código imprevisível.
- As regras validam formato permitido, quantidade inteira de pelo menos 1 e campos de propriedade imutáveis.
- Criação e aceite gravam diretamente no Firestore em operações atômicas; as regras exigem que convite, participação e lista mudem juntos e com campos imutáveis preservados.

## Erros Visíveis ao Usuário

| Código | Situação |
|---|---|
| LIST_CLOSED | A lista foi encerrada antes da confirmação da ação. |
| INVITATION_EXPIRED | O convite passou de 3 horas. |
| INVITATION_UNAVAILABLE | O convite já foi aceito, invalidado ou a lista foi encerrada. |
| NOT_AUTHORIZED | O usuário não participa da lista ou não tem o papel necessário. |
| INVALID_ITEM | Nome ausente ou quantidade abaixo de 1. |
| NETWORK_UNAVAILABLE | A alteração não foi confirmada; o usuário pode tentar novamente. |
