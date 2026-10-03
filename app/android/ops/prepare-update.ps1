# Produces publication files from an already signed website release. Does not upload anything.
[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][string]$Apk,
    [Parameter(Mandatory=$true)][string]$BuildTools,
    [Parameter(Mandatory=$true)][string]$Notes,
    [string]$OutputDirectory
)
$ErrorActionPreference = 'Stop'
$apkFile = Get-Item -LiteralPath $Apk
if ($apkFile.Extension -ne '.apk') { throw 'Wskaż plik APK.' }
$signer = Join-Path $BuildTools 'apksigner.bat'
$aapt = Join-Path $BuildTools 'aapt.exe'
if (!(Test-Path -LiteralPath $signer) -or !(Test-Path -LiteralPath $aapt)) { throw 'Wskaż katalog build-tools z Android SDK.' }
$signature = (& $signer verify --verbose --print-certs $apkFile.FullName 2>&1 | Out-String)
if ($LASTEXITCODE -ne 0) { throw 'APK nie ma prawidłowego podpisu. Najpierw podpisz wydanie swoim dotychczasowym kluczem.' }
if ($signature -match 'CN=Android Debug') { throw 'Nie publikuj APK podpisanego kluczem testowym Androida.' }
$badging = (& $aapt dump badging $apkFile.FullName 2>&1 | Out-String)
if ($LASTEXITCODE -ne 0) { throw 'Nie można odczytać danych APK.' }
$package = [regex]::Match($badging, "package: name='([^']+)' versionCode='([0-9]+)' versionName='([^']+)'")
$sdk = [regex]::Match($badging, "sdkVersion:'([0-9]+)'")
if (!$package.Success -or !$sdk.Success) { throw 'Brak danych wersji w APK.' }
if ($package.Groups[1].Value -ne 'eu.polanieonline.client' -or $badging -match 'application-debuggable') { throw 'To nie jest produkcyjne APK PolanieOnLine.' }
if ($badging -notmatch "uses-permission: name='android.permission.REQUEST_INSTALL_PACKAGES'") { throw 'To nie jest wydanie ze strony z updaterem. Wariant Google Play nie powinien być publikowany przez ten kanał.' }
$code = [int]$package.Groups[2].Value
if ($code -le 0 -or $code -gt 2100000000) { throw 'Nieprawidłowy versionCode.' }
if ($Notes.Length -gt 12000 -or [string]::IsNullOrWhiteSpace($Notes)) { throw 'Podaj krótki opis zmian.' }
if ($apkFile.Length -le 0 -or $apkFile.Length -gt 150MB) { throw 'Nieprawidłowy rozmiar APK.' }
if (!$OutputDirectory) { $OutputDirectory = Join-Path $PSScriptRoot "../../../build/android-update-$code" }
$destination = [IO.Path]::GetFullPath($OutputDirectory)
if (Test-Path -LiteralPath $destination) { throw 'Katalog docelowy już istnieje. Wybierz nowy OutputDirectory, aby nie nadpisać wydania.' }
$filename = "polanieonline-$code.apk"
$manifest = [ordered]@{
    schema = 1
    enabled = $true
    package_name = 'eu.polanieonline.client'
    version_code = $code
    version_name = $package.Groups[3].Value
    min_sdk = [int]$sdk.Groups[1].Value
    size_bytes = $apkFile.Length
    apk_url = "https://polanieonline.eu/client/android/$filename"
    sha256 = (Get-FileHash -LiteralPath $apkFile.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    notes = $Notes
}
$json = $manifest | ConvertTo-Json -Depth 5
if ([Text.Encoding]::UTF8.GetByteCount($json) -gt 32768) { throw 'Opis wydania jest zbyt duży. Skróć opis zmian.' }
New-Item -ItemType Directory -Path $destination | Out-Null
Copy-Item -LiteralPath $apkFile.FullName -Destination (Join-Path $destination $filename)
[IO.File]::WriteAllText((Join-Path $destination 'update.json'), $json, [Text.UTF8Encoding]::new($false))
Write-Output "Przygotowano: $destination"
Write-Output 'Opublikuj APK w /client/android/. Następnie opublikuj update.json jako ostatni plik.'
Write-Output 'Nie zmieniaj klucza podpisu. Nie publikuj nieukończonego ani testowego wydania.'
