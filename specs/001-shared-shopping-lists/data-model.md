# Modelo de Dados

Os horários são armazenados e avaliados em UTC. Identificadores de convites são códigos aleatórios de 128 bits representados por 32 caracteres hexadecimais.

## Usuário

Documento: users/{uid}

| Campo | Regra |
|---|---|
| email | Obrigatório, normalizado e único na autenticação |
| displayName | Nome exibido ao grupo |
| createdAt | Horário definido pelo servidor |

## Lista de Compras

Documento: lists/{listId}

| Campo | Regra |
|---|---|
| name | Obrigatório, entre 1 e 100 caracteres após remoção de espaços externos |
| ownerId | UID do criador; imutável |
| memberIds | UIDs dos participantes, usado para consultar somente listas autorizadas |
| status | ACTIVE ou CLOSED |
| createdAt, updatedAt | Horários definidos pelo servidor |
| closedAt | Obrigatório apenas em CLOSED |

Subcoleção: lists/{listId}/members/{uid}

| Campo | Regra |
|---|---|
| role | OWNER ou MEMBER |
| displayName | Nome do participante exibido na lista e na autoria de marcações |
| joinedAt | Horário definido pelo servidor |

O proprietário sempre possui um documento de participação com papel OWNER.

## Item da Lista

Documento: lists/{listId}/items/{normalizedName}

| Campo | Regra |
|---|---|
| name | Nome exibido; obrigatório |
| normalizedName | Nome sem espaços externos, com espaços internos normalizados e sem distinção de maiúsculas/minúsculas; também é o ID do documento |
| quantity | Inteiro obrigatório, mínimo 1 |
| inCart | Booleano; inicia como false |
| lastMarkedByUserId | UID de quem marcou o item; nulo quando pendente |
| updatedAt, updatedByUserId | Última alteração confirmada pelo servidor |

Adicionar um nome equivalente ao de um item existente aumenta quantity em 1 na mesma transação. Não podem existir itens duplicados por nome normalizado.

## Convite

Documento: invitations/{inviteId}

| Campo | Regra |
|---|---|
| listId | Lista alvo |
| inviterId | UID do proprietário que convidou |
| inviterDisplayName | Nome do proprietário exibido ao destinatário |
| status | PENDING, ACCEPTED, EXPIRED ou INVALIDATED |
| createdAt | Horário definido pelo servidor |
| expiresAt | createdAt + 3 horas |
| acceptedAt, acceptedByUserId | Preenchidos somente ao aceitar |

## Transições de Estado

Lista: ACTIVE → CLOSED  
Convite: PENDING → ACCEPTED, EXPIRED ou INVALIDATED  
Item: PENDING ↔ IN_CART

O encerramento invalida logicamente convites pendentes. Aceitar um convite é uma transação: valida código, estado pendente, expiração e lista ativa; cria a participação; atualiza a lista; e marca o convite como aceito.
