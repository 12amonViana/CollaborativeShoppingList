# Solução de problemas

## O APK não instala

### Aplicativos desconhecidos bloqueados

Autorize temporariamente a instalação para o navegador ou gerenciador de arquivos usado para abrir o APK.

### Assinaturas incompatíveis

A mensagem `INSTALL_FAILED_UPDATE_INCOMPATIBLE` indica que a versão instalada e o novo APK foram assinados com chaves diferentes. Para preservar dados, obtenha um APK assinado com a mesma chave. Desinstalar resolve a incompatibilidade, mas apaga os dados locais.

### Android incompatível

O aplicativo exige Android 8.0 ou superior.

## Não consigo aceitar um convite

Verifique se:

- o código foi digitado exatamente;
- o código ainda está dentro do prazo de 3 horas;
- outra pessoa ainda não o utilizou;
- a lista continua ativa;
- há conexão com a internet.

Peça ao proprietário para gerar um novo código se necessário.

## Alterações não aparecem em outro dispositivo

Confirme que ambos os dispositivos estão conectados à internet, usam contas participantes da mesma lista e executam uma versão configurada para produção. Feche e abra a lista novamente se a conexão tiver sido interrompida.

## Não consigo reutilizar ou renomear

O proprietário não pode reativar ou renomear de forma que existam duas listas ativas próprias com nomes equivalentes. Renomeie uma das listas antes de tentar novamente.

## O Firebase Emulator não inicia

- Confirme que Java 21 ou superior está no `PATH`.
- Verifique se as portas 8080, 9099, 4000, 4400 e 9150 estão livres.
- Encerre instâncias antigas do Emulator Suite antes de iniciar outra.

## A operação foi negada

Erros de permissão normalmente indicam que a conta não possui o papel necessário, saiu da lista, a lista foi encerrada ou as regras publicadas não correspondem à versão do aplicativo.
