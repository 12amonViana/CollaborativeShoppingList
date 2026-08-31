# Especificação da Funcionalidade: Listas de Compras Compartilhadas

**Branch da Funcionalidade**: `001-shared-shopping-lists`

**Criada em**: 2026-08-29

**Status**: Implementada — validação técnica concluída

**Entrada**: Definição consolidada do produto e do MVP.

## Contexto do Produto

Aplicativo Android para criar e compartilhar listas de compras em tempo real. Ele permite que várias pessoas acompanhem o andamento de uma compra, saibam quais itens já foram colocados no carrinho e coordenem alterações na mesma lista.

## Escopo do MVP

Esta primeira versão inclui criação de conta e entrada, criação e encerramento de listas, adição, alteração e remoção de itens, controle de quantidade, marcação de itens no carrinho, compartilhamento por convite, aceitação de convite, visualização dos participantes e atualizações em tempo real.

Ficam fora do MVP integrações com supermercados, preços e comparação de preços, leitura de código de barras, recursos de inteligência artificial, sugestões automáticas de produtos, localização do supermercado, pagamentos, histórico avançado de compras, notificações push e sincronização com calendário.

## Esclarecimentos

### Sessão 2026-08-29

- P: Como alterações simultâneas no mesmo item devem ser resolvidas? → R: A última alteração confirmada prevalece, e seu estado resultante é exibido a todos os participantes.
- P: Por quanto tempo um convite é válido e o que ocorre caso sua lista seja fechada? → R: Um convite expira após 3 horas; convites pendentes de uma lista fechada não podem ser aceitos.
- P: Quais regras de validação e de itens duplicados se aplicam aos itens da lista? → R: Nome e quantidade são obrigatórios; novos itens começam com quantidade 1; a quantidade não pode ficar abaixo de 1; adicionar um nome duplicado aumenta a quantidade do item existente.

## Cenários de Usuário e Testes *(obrigatório)*

### História de Usuário 1 - Gerenciar uma lista pessoal (Prioridade: P1)

Como uma pessoa compradora registrada, quero criar uma lista de compras e gerenciar seus itens para organizar uma compra antes de ir ao supermercado.

**Por que esta prioridade**: Uma lista útil é a base para todas as demais capacidades do MVP.

**Teste independente**: Uma pessoa registrada cria uma lista, adiciona itens, altera uma quantidade, remove um item e verifica a lista resultante.

**Cenários de aceitação**:

1. **Dado** um usuário autenticado, **Quando** cria uma lista com um nome, **Então** a lista fica disponível para ele como proprietário.
2. **Dado** uma lista ativa, **Quando** um participante adiciona um item nomeado, **Então** ele é adicionado com quantidade 1 e todos os participantes podem vê-lo.
3. **Dado** um item em uma lista ativa, **Quando** um participante altera sua quantidade ou o remove, **Então** a lista atualizada é exibida para todos os participantes.

---

### História de Usuário 2 - Acompanhar itens durante uma compra (Prioridade: P1)

Como participante de uma lista de compras, quero marcar itens como colocados no carrinho para que o grupo saiba o que ainda falta comprar.

**Por que esta prioridade**: Ela entrega o valor de acompanhar o progresso da compra em tempo real.

**Teste independente**: Um participante marca e desmarca um item, e a lista registra visivelmente seu estado atual e o participante que o marcou por último.

**Cenários de aceitação**:

1. **Dado** um item desmarcado em uma lista ativa, **Quando** um participante o marca como colocado no carrinho, **Então** o item é exibido como marcado e identifica esse participante.
2. **Dado** um item marcado, **Quando** um participante o desmarca, **Então** o item volta a ser exibido como pendente.
3. **Dado** dois participantes visualizando a mesma lista ativa, **Quando** um deles altera o status de um item, **Então** o outro vê a alteração sem atualizar manualmente a lista.

---

### História de Usuário 3 - Compartilhar e participar de uma lista (Prioridade: P1)

Como proprietário de uma lista, quero gerar um código temporário para compartilhá-la com outra pessoa e coordenarmos a mesma compra sem serviços pagos.

**Por que esta prioridade**: A colaboração diferencia o produto de um aplicativo de lista de compras individual.

**Teste independente**: Um proprietário gera e compartilha um código, outro usuário autenticado o informa no aplicativo e ambos visualizam a mesma lista e seus participantes.

**Cenários de aceitação**:

1. **Dado** uma lista ativa pertencente a um usuário, **Quando** o proprietário gera um convite, **Então** recebe um código secreto e imprevisível que pode compartilhar por um meio de sua confiança.
2. **Dado** um código de convite pendente, **Quando** um usuário autenticado o informa no aplicativo, **Então** ele obtém acesso à lista ativa e passa a constar entre seus participantes.
3. **Dado** uma lista ativa compartilhada, **Quando** qualquer participante altera um item, **Então** todos os participantes atuais veem a alteração em tempo real.
4. **Dado** um convite pendente, **Quando** passam-se 3 horas ou sua lista associada é fechada, **Então** o convite não pode ser aceito.

---

### História de Usuário 4 - Encerrar uma lista concluída (Prioridade: P2)

Como proprietário de uma lista, quero encerrá-la ao concluir a compra para que os participantes saibam que ela não está mais em uso.

**Por que esta prioridade**: Ela fornece um estado final claro, mantendo o foco do MVP.

**Teste independente**: O proprietário encerra uma lista e todos os participantes veem que ela foi encerrada e não podem mais alterar seus itens.

**Cenários de aceitação**:

1. **Dado** uma lista ativa pertencente ao usuário, **Quando** o proprietário a encerra, **Então** seu status passa a ser encerrado para todos os participantes.
2. **Dado** uma lista encerrada, **Quando** um participante tenta adicionar, remover, editar, marcar ou desmarcar um item, **Então** a alteração é recusada e a lista permanece inalterada.

### Casos de Borda

- Um convite não pode conceder acesso se já tiver sido aceito, expirado ou estiver associado a uma lista encerrada; conhecer o código é a autorização para um único usuário autenticado aceitá-lo.
- Se dois participantes alterarem o mesmo item quase simultaneamente, a última alteração confirmada pelo sistema será o estado final único exibido para todos os participantes.
- Um usuário que não seja participante atual não pode visualizar nem alterar uma lista compartilhada.
- Se um item for removido após ter sido marcado, ele deixa de aparecer na lista e em seu progresso.
- Se um participante adicionar um item cujo nome já exista na lista, o sistema aumenta a quantidade do item existente em vez de criar um duplicado.
- Um item não pode ter nome ausente, quantidade ausente, quantidade zero ou negativa; uma diminuição de quantidade para em 1.
- Interrupções de conectividade indicam claramente que uma atualização não foi aplicada, e o usuário pode tentar novamente após reconectar.

## Requisitos *(obrigatório)*

### Requisitos Funcionais

- **RF-001**: O sistema DEVE permitir que uma pessoa crie uma conta e entre antes de acessar listas de compras.
- **RF-002**: O sistema DEVE permitir que um usuário autenticado crie uma lista de compras nomeada e se torne seu proprietário.
- **RF-003**: O sistema DEVE permitir que participantes atuais de uma lista ativa adicionem itens nomeados, alterem uma quantidade e removam itens; cada novo item DEVE começar com quantidade 1.
- **RF-003a**: O sistema DEVE exigir um nome de item e uma quantidade de pelo menos 1; ele DEVE impedir que uma diminuição deixe a quantidade abaixo de 1.
- **RF-003b**: Quando um participante adicionar um item com o mesmo nome de um item existente na mesma lista, o sistema DEVE aumentar a quantidade do item existente em vez de criar um duplicado.
- **RF-004**: O sistema DEVE permitir que participantes atuais de uma lista ativa marquem e desmarquem itens como colocados no carrinho.
- **RF-005**: O sistema DEVE mostrar se cada item está pendente ou marcado e identificar o participante que marcou um item pela última vez.
- **RF-006**: O sistema DEVE tornar adições, remoções, alterações de quantidade e alterações de status dos itens visíveis a todos os participantes atuais sem atualização manual.
- **RF-007**: O sistema DEVE permitir que o proprietário de uma lista ativa gere um código de convite secreto, imprevisível e compartilhável, sem depender de serviços com cobrança por uso.
- **RF-008**: O sistema DEVE permitir que um usuário autenticado aceite uma única vez um código de convite pendente e, ao aceitá-lo, obtenha acesso à lista ativa associada.
- **RF-008a**: O sistema DEVE expirar um convite pendente 3 horas após sua emissão e DEVE impedir a aceitação de um convite cuja lista associada esteja encerrada.
- **RF-009**: O sistema DEVE mostrar os participantes atuais de cada lista aos seus participantes.
- **RF-010**: O sistema DEVE permitir somente ao proprietário encerrar uma lista ativa.
- **RF-011**: O sistema DEVE impedir alterações nos itens de uma lista encerrada e exibir claramente que ela está encerrada.
- **RF-012**: O sistema DEVE restringir a visualização e as alterações de uma lista ao proprietário e aos participantes aceitos.
- **RF-012a**: Quando alterações simultâneas afetarem o mesmo item, o sistema DEVE preservar e distribuir a última alteração que confirmar como estado final do item.
- **RF-013**: O MVP NÃO DEVE incluir integrações com supermercados, preços ou comparação de preços, leitura de código de barras, recursos de inteligência artificial, sugestões automáticas de produtos, localização do supermercado, pagamentos, histórico avançado de compras, notificações push ou sincronização com calendário.

### Entidades Principais *(incluir se a funcionalidade envolver dados)*

- **Usuário**: Pessoa registrada que pode possuir, participar de ou ser convidada para listas de compras.
- **Lista de Compras**: Coleção nomeada de itens com um proprietário, uma lista de participantes e status ativa ou encerrada.
- **Item da Lista**: Entrada com nome único em uma lista de compras, quantidade de pelo menos 1, status no carrinho e participante que o marcou por último.
- **Convite**: Código secreto de uso único gerado por um proprietário para acesso a uma lista ativa específica; possui pelo menos 128 bits de aleatoriedade e expira 3 horas após ser emitido.
- **Participação**: Acesso aceito de um usuário a uma lista de compras específica.

## Critérios de Sucesso *(obrigatório)*

### Resultados Mensuráveis

- **CS-001**: Pelo menos 90% dos usuários de primeira viagem conseguem criar uma conta, criar uma lista e adicionar seu primeiro item em até 3 minutos, sem ajuda.
- **CS-002**: Pelo menos 95% das adições, edições, remoções e alterações de status dos itens ficam visíveis a outros participantes online em até 3 segundos.
- **CS-003**: Pelo menos 95% dos usuários convidados conseguem aceitar um convite e acessar a lista compartilhada em até 1 minuto após abri-lo.
- **CS-004**: Em testes de usabilidade, pelo menos 90% dos participantes conseguem identificar sem ajuda quais itens permanecem pendentes e quem marcou um item concluído.
- **CS-005**: Pelo menos 95% das tentativas de alterar uma lista encerrada são impedidas, preservando seu estado final.

### Protocolo de Validação dos Critérios de Sucesso

- A validação formal DEVE usar a mesma versão candidata do aplicativo e registrar, para cada execução, o dispositivo, a condição de conectividade, o tempo medido e o resultado obtido. O relatório consolidado DEVE informar o tamanho da amostra, sucessos, falhas e percentual final de cada critério.
- **CS-001**: Avaliar no mínimo 20 pessoas que não participaram do desenvolvimento e nunca utilizaram o aplicativo. A medição começa quando a tela de criação de conta é exibida e termina quando o primeiro item aparece na lista criada. Não é permitida ajuda durante a execução.
- **CS-002**: Executar no mínimo 100 alterações entre dois participantes online, distribuídas entre adição, edição de quantidade, remoção e mudança de status. Medir da confirmação visível no dispositivo de origem até a atualização visível no outro dispositivo; pelo menos 95 alterações devem aparecer em até 3 segundos.
- **CS-003**: Avaliar no mínimo 20 usuários convidados que não tenham realizado antes o fluxo de aceite. A medição começa quando o convite é aberto e termina quando a lista compartilhada fica acessível; não é permitida ajuda durante a execução.
- **CS-004**: Avaliar no mínimo 20 pessoas que não participaram do desenvolvimento. Cada pessoa recebe uma lista contendo pelo menos um item pendente e um item marcado por um participante identificado, e deve apontar corretamente ambos sem ajuda.
- **CS-005**: Executar no mínimo 100 tentativas de alteração após o encerramento de listas, distribuídas entre adicionar, editar quantidade, remover, marcar e desmarcar itens. O relatório deve confirmar quantas tentativas foram recusadas e que nenhuma recusa alterou o estado final da lista.
- A validação técnica com duas contas e dois dispositivos comprova os fluxos funcionais do MVP, mas não substitui as amostras estatísticas deste protocolo para uma decisão de lançamento público.

## Premissas

- O MVP é direcionado a usuários Android com conexão à internet; falhas temporárias de conexão contam com um caminho claro para tentar novamente.
- Proprietários compartilham códigos de convite fora do aplicativo; quem recebe o código precisa autenticar-se antes de aceitá-lo.
- O ambiente de produção opera no plano sem faturamento vinculado; ao atingir uma cota gratuita, a indisponibilidade temporária é preferível a qualquer cobrança.
- Cada lista tem um único proprietário; transferência de propriedade e remoção de participantes estão fora do MVP.
- Encerrar uma lista a preserva para consulta pelos participantes, mas não permite mais edição.
- Nomes de itens equivalentes são comparados de modo consistente dentro de uma lista; a regra exata de normalização é uma decisão de planejamento.
- As exclusões enumeradas neste documento são limites intencionais do MVP.
