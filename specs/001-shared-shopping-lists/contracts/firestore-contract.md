# Contrato de Dados e Acesso do Firestore

## Operações do Aplicativo

| Operação | Autor | Precondições | Resultado |
|---|---|---|---|
| Criar lista | Usuário autenticado | Nome válido | Cria lista ativa e participação OWNER |
| Adicionar item | Participante | Lista ativa; nome e categoria válidos | Cria item com quantidade 1 ou soma 1 a item equivalente preservando sua categoria |
| Alterar quantidade | Participante | Lista ativa; quantidade inteira >= 1 | Atualiza a quantidade |
| Marcar/desmarcar | Participante | Lista ativa | Atualiza inCart, autoria e horário |
| Remover item | Participante | Lista ativa; item existe | Exclui o item |
| Criar convite | Proprietário | Lista ativa | Transação cria código aleatório pendente de 3 horas |
| Aceitar convite | Usuário autenticado com o código | Convite pendente, não expirado; lista ativa | Transação cria participação, atualiza a lista e aceita convite |
| Encerrar lista | Proprietário | Lista ativa | Transação fecha a lista; convites pendentes tornam-se inutilizáveis |
| Renomear lista | Proprietário | Nome válido; nenhuma outra lista ativa própria com nome equivalente | Atualiza o nome da lista |
| Reativar lista | Proprietário | Lista encerrada; nenhuma lista ativa própria com nome equivalente | Reabre a lista e reinicia todos os itens para quantidade 1 e pendentes |
| Excluir lista | Proprietário | Lista ativa ou encerrada | Remove a lista do acesso de todos os participantes |
| Abandonar lista | Participante convidado | Participação existente | Remove o próprio vínculo e preserva os demais |
| Abandonar como proprietário | Proprietário | Há outro participante | Promove o convidado mais antigo e remove o proprietário anterior |
| Abandonar lista individual | Proprietário | Não há outros participantes | Exclui a lista e seus itens |

Todas as operações de alteração são transacionais. Em alterações concorrentes do mesmo campo, a última transação confirmada pelo servidor vence.

## Eventos em Tempo Real

O aplicativo mantém listeners apenas para listas das quais o usuário é participante, seus itens, membros e os próprios convites. A interface trata cada alteração confirmada do Firestore como estado canônico e atualiza a tela em até 3 segundos para participantes online.

## Firebase Security Rules — Política

- Todas as leituras e escritas são negadas por padrão.
- Um usuário só pode ler uma lista, seus itens e membros quando possui participação.
- Apenas o proprietário pode encerrar lista e iniciar convite.
- Participantes podem alterar itens somente enquanto a lista estiver ACTIVE.
- Convites não podem ser listados; um usuário autenticado só pode buscar um documento quando já conhece seu código imprevisível.
- As regras validam categoria permitida, formato, quantidade inteira de pelo menos 1 e campos de propriedade imutáveis.
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
