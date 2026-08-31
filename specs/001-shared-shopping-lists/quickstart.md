# Guia de Validação do MVP

## Pré-requisitos

- Android Studio com um emulador Android configurado.
- Projeto Firebase Spark configurado para Authentication e Firestore, sem faturamento vinculado.
- Firebase CLI e Firebase Local Emulator Suite configurados para testes locais.
- Uma conta de teste por participante.

## Cenário 1 — Lista individual

1. Registrar uma conta e entrar.
2. Criar uma lista chamada “Compra da semana”.
3. Adicionar “Leite”; confirmar quantidade 1.
4. Adicionar “ leite ”; confirmar que o mesmo item passa a ter quantidade 2.
5. Tentar definir quantidade 0; confirmar que a alteração é recusada.

**Resultado esperado**: somente um item equivalente existe, com quantidade mínima 1.

## Cenário 2 — Colaboração em tempo real

1. Entrar com duas contas em dispositivos ou emuladores distintos.
2. Fazer a primeira conta gerar um código de convite e compartilhá-lo com a segunda.
3. Informar e aceitar o código com a segunda conta.
4. Marcar um item como colocado no carrinho com a primeira conta.

**Resultado esperado**: a segunda conta vê a marcação e o responsável em até 3 segundos.

## Cenário 3 — Convite e encerramento

1. Gerar um código e não aceitá-lo por mais de 3 horas no relógio do ambiente de teste.
2. Confirmar que ele não pode ser aceito.
3. Gerar outro código, encerrar a lista antes do aceite e tentar aceitá-lo.

**Resultado esperado**: os dois convites são recusados; a lista encerrada permanece apenas para consulta.

## Cenário 4 — Autorização e concorrência

1. Tentar abrir uma lista com uma terceira conta não participante.
2. Em duas contas participantes, alterar quase simultaneamente a quantidade do mesmo item.
3. Executar os testes de Security Rules contra o Emulator Suite.

**Resultado esperado**: a terceira conta não acessa a lista; todos os participantes convergem para a última alteração confirmada; operações sem autorização são negadas pelo backend.

Consulte data-model.md para entidades e contracts/firestore-contract.md para permissões e operações.

## Validação automatizada equivalente

Os mesmos fluxos de backend podem ser repetidos com três contas descartáveis no Emulator Suite:

```powershell
Set-Location firebase/functions
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
npm ci
npm run test:emulator
```

O resultado esperado termina com `E2E_OK`. A verificação visual em dois emuladores Android continua recomendada para conferir layout e interação por toque.
