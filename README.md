# Collaborative Shopping List

Aplicativo Android de listas de compras compartilhadas em tempo real, construído com Kotlin, Jetpack Compose, Hilt e Firebase. O MVP permite autenticar, criar listas, unir itens de nome equivalente, controlar quantidades, acompanhar o carrinho, convidar participantes e encerrar listas.

## Requisitos

- Android Studio com Android SDK 35 e JDK 17.
- Node.js 20 para Cloud Functions.
- Java 21 ou superior para o Firebase Local Emulator Suite atual. O Java incluído no Android Studio pode ser usado.
- Um projeto Firebase com Authentication por e-mail/senha, Firestore e Functions habilitados.

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

## Cloud Functions e emuladores

```powershell
Set-Location firebase/functions
npm ci
npm run build
```

Se o Java padrão for anterior ao 21, use o runtime do Android Studio antes de iniciar os emuladores:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
npx firebase emulators:start --config ../firebase.json
```

Com os emuladores ativos, execute o aplicativo em um Android Emulator. O host `10.0.2.2` configurado em `local.properties` aponta para a máquina local.

## Validação automatizada

O teste de integração inicia Auth, Firestore e Functions localmente e valida três contas, autorização, itens equivalentes, quantidade mínima, atualização em tempo real, convites, expiração, concorrência e encerramento:

```powershell
Set-Location firebase/functions
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
npm run test:emulator
```

Para a conferência visual final em dois dispositivos, siga `specs/001-shared-shopping-lists/quickstart.md`.

## Estrutura

- `android/`: aplicativo Android.
- `firebase/`: regras, índices, emuladores e Cloud Functions.
- `specs/001-shared-shopping-lists/`: especificação, plano, modelo, contrato, tarefas e quickstart.

Arquivos de credenciais, `local.properties`, caches, dependências e artefatos de build não são versionados.
