$ErrorActionPreference = 'Stop'
$signingDirectory = Join-Path $env:LOCALAPPDATA 'MeydanTV/signing'
$keystore = Join-Path $signingDirectory 'meydan-tv.p12'
$credentials = Join-Path $signingDirectory 'password.xml'
New-Item -ItemType Directory -Force -Path $signingDirectory | Out-Null
if ((Test-Path -LiteralPath $keystore) -ne (Test-Path -LiteralPath $credentials)) {
    throw 'Signing key and encrypted credential must both exist. Restore the missing file; do not regenerate the key.'
}
$previousStore = $env:MEYDAN_KEYSTORE
$previousPassword = $env:MEYDAN_STORE_PASSWORD
try {
    if (-not (Test-Path -LiteralPath $credentials)) {
        $bytes = New-Object byte[] 32
        $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
        $rng.GetBytes($bytes)
        $rng.Dispose()
        $password = [Convert]::ToBase64String($bytes)
        $secret = ConvertTo-SecureString $password -AsPlainText -Force
        $credential = New-Object Management.Automation.PSCredential('meydan-tv', $secret)
        $env:MEYDAN_STORE_PASSWORD = $password
        & keytool -genkeypair -keystore $keystore -storetype PKCS12 -alias meydan-tv -keyalg RSA -keysize 3072 -validity 10000 -dname 'CN=Meydan TV' -storepass:env MEYDAN_STORE_PASSWORD -keypass:env MEYDAN_STORE_PASSWORD
        if ($LASTEXITCODE -ne 0) { throw 'Signing key generation failed.' }
        # Windows DPAPI protects this password for the current user on this machine.
        $credential | Export-Clixml -LiteralPath $credentials
    }
    $credential = Import-Clixml -LiteralPath $credentials
    $env:MEYDAN_KEYSTORE = $keystore
    $env:MEYDAN_STORE_PASSWORD = $credential.GetNetworkCredential().Password
    Push-Location $PSScriptRoot
    try {
        & .\gradlew.bat --no-daemon assembleRelease testDebugUnitTest lintRelease
        if ($LASTEXITCODE -ne 0) { throw 'Release validation failed.' }
    } finally { Pop-Location }
    Write-Host "Release signing files: $signingDirectory"
} finally {
    $env:MEYDAN_KEYSTORE = $previousStore
    $env:MEYDAN_STORE_PASSWORD = $previousPassword
}
