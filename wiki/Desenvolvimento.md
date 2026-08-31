# Desenvolvimento

## Requisitos

- Android Studio e Android SDK 35.
- JDK 17 para o build Android.
- Node.js 20 ou superior.
- Java 21 ou superior para o Firebase Emulator Suite atual.
- Projeto Firebase com Authentication e Firestore habilitados.

## Configuração local

1. Coloque `google-services.json` em `android/app/`.
2. Copie `android/local.properties.example` para `android/local.properties`.
3. Configure `sdk.dir`.
4. Use `firebase.useEmulators=true` para desenvolvimento local ou `false` para o Firebase real.

## Compilar o APK

```powershell
Set-Location android
.\gradlew.bat testDebugUnitTest assembleDebug
```

O arquivo será criado em:

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

## Executar testes do backend

```powershell
Set-Location firebase/functions
npm ci
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
npm run test:emulator
```

O resultado esperado termina com `E2E_OK`.

## Publicar somente as regras

```powershell
Set-Location firebase
firebase deploy --only firestore:rules
```

Sempre execute os testes do Emulator Suite antes da publicação.

## Estrutura do repositório

- `android/`: aplicativo Kotlin/Compose.
- `firebase/`: regras, índices e testes do Emulator Suite.
- `specs/001-shared-shopping-lists/`: especificação, plano, modelo, contrato e tarefas.
- `wiki/`: conteúdo da documentação do GitHub Wiki.
