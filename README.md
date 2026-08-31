# Collaborative Shopping List

Aplicativo Android de listas de compras compartilhadas em tempo real, construído com Kotlin, Jetpack Compose, Hilt e Firebase no plano gratuito Spark. O MVP permite autenticar, criar listas, unir itens de nome equivalente, controlar quantidades, acompanhar o carrinho, compartilhar códigos de convite e encerrar listas.

## Requisitos

- Android Studio com Android SDK 35 e JDK 17.
- Node.js 20 para os testes do Emulator Suite.
- Java 21 ou superior para o Firebase Local Emulator Suite atual. O Java incluído no Android Studio pode ser usado.
- Um projeto Firebase Spark com Authentication por e-mail/senha e Firestore habilitados, sem conta de faturamento vinculada.

## Configuração Android

1. Coloque o arquivo do Firebase em `android/app/google-services.json`.
2. Copie `android/local.properties.example` para `android/local.properties`.
3. Ajuste `sdk.dir` para o Android SDK local.
4. Para usar os emuladores, mantenha `firebase.useEmulators=true`. Para serviços reais, use `false`.

Compile no PowerShell:

```powershell
Set-Location android
.\gradlew.bat :app:assembleDebug
```

O APK será criado em `android/app/build/outputs/apk/debug/app-debug.apk`.

## Testes e emuladores

```powershell
Set-Location firebase/functions
npm ci
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
npm run test:emulator
```

Com os emuladores ativos, execute o aplicativo em um Android Emulator. O host `10.0.2.2` configurado em `local.properties` aponta para a máquina local.

## Validação automatizada

O teste de integração inicia Auth e Firestore localmente e valida três contas, autorização, itens equivalentes, quantidade mínima, atualização em tempo real, códigos de convite, expiração e encerramento:

```powershell
Set-Location firebase/functions
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
npm run test:emulator
```

Para a conferência visual final em dois dispositivos, siga `specs/001-shared-shopping-lists/quickstart.md`.

## Estrutura

- `android/`: aplicativo Android.
- `firebase/`: regras, índices e testes dos emuladores Auth/Firestore.
- `specs/001-shared-shopping-lists/`: especificação, plano, modelo, contrato, tarefas e quickstart.

Arquivos de credenciais, `local.properties`, caches, dependências e artefatos de build não são versionados.
