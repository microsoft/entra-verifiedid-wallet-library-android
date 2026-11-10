# Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.

$ErrorActionPreference = 'Stop'
$repositoryRoot = Split-Path -Parent $PSScriptRoot

Push-Location -LiteralPath $repositoryRoot
try {
    & (Join-Path $repositoryRoot 'gradlew.bat') :walletlibrary:assembleDebug :walletlibrarydemo:assembleDebug :walletlibrary:testDebugUnitTest :walletlibrary:lintDebug :walletlibrarydemo:lintDebug
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle verification failed with exit code $LASTEXITCODE."
    }
}
finally {
    Pop-Location
}
