<!-- Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License. -->

# Android Wallet Library: agent entry point

This repository is `microsoft/entra-verifiedid-wallet-library-android`, not the
MSAL library or the iOS Wallet Library. Read the [README](README.md) for the public
API, issuance/presentation flows, and consumer integration.

- [walletlibrary](walletlibrary/src/main/java/com/microsoft/walletlibrary)
  contains the library, including DID/cryptography, storage, and network code.
- [walletlibrarydemo](walletlibrarydemo/src/main) is the sample consumer.
- [settings.gradle](settings.gradle) declares both modules.
- [httpAgent.md](httpAgent.md) documents HTTP agent integration.
- Follow [SECURITY.md](SECURITY.md) for reporting security issues.

Preserve public APIs, credential validation, issuer/verifier trust checks, and
key/storage behavior. Use existing Kotlin, coroutine, logging, and test patterns.
Do not log credentials, private keys, tokens, or personal claims. Keep changes
focused and do not weaken checks or alter dependencies to make validation pass.

## Setup

Run commands from the repository root using the checked-in
[Gradle 8.7 wrapper](gradle/wrapper/gradle-wrapper.properties).
Use **JDK 17**, matching [CI](.github/workflows/ci.yml), and configure `JAVA_HOME`.
Install Android SDK platform 34 and Build Tools 34.0.0, accept their licenses,
and set `ANDROID_HOME` or an untracked `local.properties` with `sdk.dir`.
Google Maven, Maven Central, and JitPack must be reachable.

[gradle.properties](gradle.properties) is the source of truth for SDK/tool
versions: the current minimum SDK is **27**, compile/target SDK is **34**,
and the JVM target is **17**. Preserve these values unless explicitly changing
platform support. Do not copy the MSA SDK's legacy Java/SDK setup.

## Validation entry point

On Windows, from the repository root:

```powershell
pwsh -File .\scripts\verify.ps1
```

The [verification gate](scripts/verify.ps1) runs library and demo debug builds,
the complete library debug JVM suite, and debug lint for both modules:

```powershell
.\gradlew.bat :walletlibrary:assembleDebug :walletlibrarydemo:assembleDebug :walletlibrary:testDebugUnitTest :walletlibrary:lintDebug :walletlibrarydemo:lintDebug
```

On macOS/Linux, run the same tasks using `./gradlew` instead of `.\gradlew.bat`.
The script propagates Gradle failures and restores the caller's working
directory. It does not publish artifacts, install apps, or run device tests.

**Lint limitation:** [walletlibrary/build.gradle](walletlibrary/build.gradle)
currently sets `abortOnError false` and `warningsAsErrors false`. A successful
Gradle exit is not proof of zero lint findings; inspect and report the lint
output and each module's `build/reports/lint-results-debug.html`.
Do not silently change the existing lint policy as part of readiness work.

## Test entry point and fast loop

Run the same library JVM suite as CI:

```powershell
.\gradlew.bat :walletlibrary:testDebugUnitTest --no-daemon --stacktrace
```

Tests live in [walletlibrary/src/test](walletlibrary/src/test), using JUnit,
MockK, coroutine test patterns, and Robolectric as applicable. Reuse an existing
test such as
[VerifiedIdClientTest](walletlibrary/src/test/java/com/microsoft/walletlibrary/VerifiedIdClientTest.kt).
For a focused iteration:

```powershell
.\gradlew.bat :walletlibrary:testDebugUnitTest --tests "com.microsoft.walletlibrary.VerifiedIdClientTest"
```

A filtered run is not the full suite. Results are under
`walletlibrary/build/test-results/testDebugUnitTest` and
`walletlibrary/build/reports/tests/testDebugUnitTest`.

## Device, integration, and consumer validation

[Instrumented tests](walletlibrary/src/androidTest) exercise Android-specific
storage, cryptography, database, and client construction behavior. JVM tests do
not replace these checks. With an approved, dedicated emulator/device (API 27
or newer) visible to `adb devices`, run:

```powershell
.\gradlew.bat :walletlibrary:connectedDebugAndroidTest
```

Use the
[encrypted keystore test](walletlibrary/src/androidTest/java/com/microsoft/walletlibrary/did/sdk/crypto/keyStore/EncryptedKeyStoreInstrumentedTest.kt)
as one example of a device-only surface. Reports are under
`walletlibrary/build/reports/androidTests/connected`.

For public API or issuance/presentation integration changes, also build the
demo through the gate and coordinate manual demo validation with approved test
issuers/verifiers and test credentials. Assemble does not execute the app or
prove end-to-end issuance/presentation works. Do not use production credentials,
erase device state, or publish packages without approval.

## Before pushing

- Run `git diff --check` and the verification gate for executable changes.
- For documentation-only changes, validate local links and script syntax;
  test script failure propagation if changing the gate.
- Run relevant device/integration checks for Android storage, key, database,
  or protocol changes and report any not-run layers.
- Report exact commands, failures, skipped checks, and lint findings. Missing
  setup/dependencies or inaccessible services are failures/blockers, not passes.
- Discover additional tasks with `.\gradlew.bat tasks --all`; discovery itself
  requires working SDK/dependency setup.

These are repository validation instructions, not an official Northstar
assessment or a certification of coverage.
