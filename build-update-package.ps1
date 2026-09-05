param(
    [switch]$NoDiff,
    [switch]$PromoteOnly,
    [switch]$ForcePromote
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version 2.0

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $Root

$BuildProperties = Join-Path $Root "build.ant.properties"
$BuildArchiveDir = Join-Path $Root "build-archive"
$HistoryManifestPath = Join-Path $BuildArchiveDir "update-1.16.properties"

function Get-BuildVersions {
    $current = $null
    $old = $null

    foreach ($line in Get-Content -LiteralPath $BuildProperties) {
        if ($line -match '^\s*version\.old\s*=\s*(.*?)\s*$') {
            $old = $Matches[1]
            continue
        }

        if ($line -match '^\s*version\s*=\s*(.*?)\s*$') {
            $current = $Matches[1]
        }
    }

    if ([string]::IsNullOrWhiteSpace($current)) {
        throw "Nie znaleziono 'version = ...' w build.ant.properties."
    }

    [pscustomobject]@{
        Current = $current.Trim()
        Old = if ($old) { $old.Trim() } else { $null }
    }
}

function Read-SimpleProperties([string]$Path) {
    $result = [ordered]@{}

    foreach ($line in Get-Content -LiteralPath $Path) {
        $trimmed = $line.Trim()

        if ($trimmed.Length -eq 0 -or
            $trimmed.StartsWith("#") -or
            $trimmed.StartsWith("!")) {
            continue
        }

        $idx = $line.IndexOf("=")
        if ($idx -lt 1) {
            continue
        }

        $key = $line.Substring(0, $idx).Trim()
        $value = $line.Substring($idx + 1).Trim()
        $result[$key] = $value
    }

    return ,$result
}

function Write-AsciiLines([string]$Path, [string[]]$Lines) {
    $encoding = [System.Text.Encoding]::ASCII
    [System.IO.File]::WriteAllLines($Path, $Lines, $encoding)
}

function Resolve-JavaTool([string]$Name) {
    if ($env:JAVA_HOME) {
        $candidate = Join-Path $env:JAVA_HOME ("bin\" + $Name + ".exe")
        if (Test-Path -LiteralPath $candidate) {
            return $candidate
        }
    }

    $cmd = Get-Command ($Name + ".exe") -ErrorAction SilentlyContinue
    if (-not $cmd) {
        $cmd = Get-Command $Name -ErrorAction SilentlyContinue
    }

    if (-not $cmd) {
        throw "Nie znaleziono narzedzia '$Name'. Ustaw JAVA_HOME."
    }

    return $cmd.Source
}

function Invoke-Checked([string]$Program, [string[]]$Arguments) {
    Write-Host ""
    Write-Host (">> " + $Program + " " + ($Arguments -join " "))
    Write-Host ""

    & $Program @Arguments

    if ($LASTEXITCODE -ne 0) {
        throw "Polecenie zakonczylo sie kodem $LASTEXITCODE."
    }
}

function Assert-JarVersion(
    [string]$Javap,
    [string]$Jar,
    [string]$ClassName,
    [string]$ExpectedVersion
) {
    $output = & $Javap -classpath $Jar -constants $ClassName 2>&1
    $exitCode = $LASTEXITCODE

    if ($exitCode -ne 0) {
        $output | ForEach-Object { Write-Host "$_" }
        throw "javap nie mogl sprawdzic $ClassName w $Jar."
    }

    $joined = ($output | ForEach-Object { "$_" }) -join "`n"
    $pattern = 'VERSION\s*=\s*"' + [regex]::Escape($ExpectedVersion) + '"'

    if ($joined -notmatch $pattern) {
        $output | Select-String "VERSION" | ForEach-Object { Write-Host "$_" }
        throw "$ClassName nie zawiera VERSION=$ExpectedVersion. Przerywam przed podpisywaniem."
    }

    Write-Host "[OK] $ClassName -> $ExpectedVersion"
}

function Get-FileSha256([string]$Path) {
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash
}

function Promote-Archive(
    [string]$Version,
    [string]$PackageRoot,
    [switch]$Force
) {
    $archiveSourceDir = Join-Path $PackageRoot "archive"
    $zipSource = Join-Path $archiveSourceDir ("polanieonline-" + $Version + ".zip")
    $manifestSource = Join-Path $archiveSourceDir "update-1.16.properties"

    if (-not (Test-Path -LiteralPath $zipSource)) {
        throw "Brakuje $zipSource. Najpierw zbuduj i przetestuj paczke."
    }

    if (-not (Test-Path -LiteralPath $manifestSource)) {
        throw "Brakuje $manifestSource. Najpierw zbuduj i przetestuj paczke."
    }

    New-Item -ItemType Directory -Path $BuildArchiveDir -Force | Out-Null

    $zipTarget = Join-Path $BuildArchiveDir ("polanieonline-" + $Version + ".zip")

    if (Test-Path -LiteralPath $zipTarget) {
        $oldHash = Get-FileSha256 $zipTarget
        $newHash = Get-FileSha256 $zipSource

        if (($oldHash -ne $newHash) -and (-not $Force)) {
            throw @"
Archiwum $zipTarget juz istnieje i ma inny SHA256.
Jesli naprawde chcesz je zastapic, uzyj:
  .\build-update-package.ps1 -PromoteOnly -ForcePromote
"@
        }
    }

    Copy-Item -LiteralPath $zipSource -Destination $zipTarget -Force
    Copy-Item -LiteralPath $manifestSource -Destination $HistoryManifestPath -Force

    Write-Host ""
    Write-Host "=========================================="
    Write-Host "PROMOCJA ARCHIWUM ZAKONCZONA"
    Write-Host "=========================================="
    Write-Host "Wersja: $Version"
    Write-Host "ZIP:     $zipTarget"
    Write-Host "Historia: $HistoryManifestPath"
    Write-Host ""
    Write-Host "To jest teraz baza do diffow dla nastepnej wersji."
}

function Restore-BuildArchiveAfterJarDiff([string]$HistoryRaw) {
    if (-not (Test-Path -LiteralPath $BuildArchiveDir)) {
        return
    }

    # jardiff rozpakowuje poprzedni ZIP bezposrednio do build-archive.
    # Zostawiamy tylko ZIP-y i przywracamy oficjalny manifest historii.
    Get-ChildItem -LiteralPath $BuildArchiveDir -Force |
        Where-Object {
            (-not $_.PSIsContainer -and $_.Extension -ne ".zip" -and $_.Name -ne "update-1.16.properties") -or
            $_.PSIsContainer
        } |
        Remove-Item -Recurse -Force

    if ($null -ne $HistoryRaw) {
        [System.IO.File]::WriteAllText(
            $HistoryManifestPath,
            $HistoryRaw,
            [System.Text.Encoding]::ASCII
        )
    }
}

$versions = Get-BuildVersions
$Version = $versions.Current
$OldVersion = $versions.Old

$PackageRoot = Join-Path $Root ("build\updates\" + $Version)
$UploadDir = Join-Path $PackageRoot "upload"
$ArchiveOutDir = Join-Path $PackageRoot "archive"

if ($PromoteOnly) {
    Promote-Archive -Version $Version -PackageRoot $PackageRoot -Force:$ForcePromote
    exit 0
}

if ($NoDiff -and $ForcePromote) {
    throw "-ForcePromote ma sens tylko razem z -PromoteOnly."
}

$Ant = Join-Path $Root "ant.cmd"
if (-not (Test-Path -LiteralPath $Ant)) {
    throw "Brakuje ant.cmd w katalogu repo."
}

$Java = Resolve-JavaTool "java"
$Javap = Resolve-JavaTool "javap"

if (-not (Test-Path -LiteralPath (Join-Path $Root "keystore.ks"))) {
    throw "Brakuje keystore.ks. Nie da sie podpisac startera i plikow updatera."
}

if (-not (Test-Path -LiteralPath (Join-Path $Root "build.ant-private.properties"))) {
    throw "Brakuje build.ant-private.properties."
}

$HistoryRaw = $null
$HistoryProperties = [ordered]@{}
$OldZip = $null

if (-not $NoDiff) {
    if ([string]::IsNullOrWhiteSpace($OldVersion)) {
        throw "Brakuje 'version.old = ...' w build.ant.properties."
    }

    if ($OldVersion -eq $Version) {
        throw "version.old i version nie moga byc takie same."
    }

    $OldZip = Join-Path $BuildArchiveDir ("polanieonline-" + $OldVersion + ".zip")

    if (-not (Test-Path -LiteralPath $OldZip)) {
        throw @"
Brakuje oficjalnego archiwum poprzedniej wersji:
  $OldZip

Dla pierwszej wersji z dzialajacym updaterem uruchom:
  .\build-update-package.ps1 -NoDiff

Dla kolejnych wersji najpierw musi byc wypromowane archiwum poprzedniego wydania.
"@
    }

    if (-not (Test-Path -LiteralPath $HistoryManifestPath)) {
        throw @"
Brakuje historii updatera:
  $HistoryManifestPath

Powinna pochodzic z oficjalnej poprzedniej wersji.
Po przetestowaniu wydania promuje sie ja poleceniem:
  .\build-update-package.ps1 -PromoteOnly
"@
    }

    $HistoryRaw = Get-Content -LiteralPath $HistoryManifestPath -Raw
    $HistoryProperties = Read-SimpleProperties $HistoryManifestPath

    $currentHistoryVersions = @(
        $HistoryProperties.Keys |
        Where-Object {
            ($_ -match '^version\.(?!destination\.)') -and
            ($HistoryProperties[$_] -eq "CURRENT")
        } |
        ForEach-Object { $_.Substring("version.".Length) }
    )

    if ($currentHistoryVersions.Count -ne 1) {
        throw "Manifest historii powinien miec dokladnie jedna wersje CURRENT."
    }

    if ($currentHistoryVersions[0] -ne $OldVersion) {
        throw @"
Niezgodnosc historii updatera:
  version.old               = $OldVersion
  CURRENT w historii        = $($currentHistoryVersions[0])

Nie wolno generowac diffow z innej bazy niz oficjalna poprzednia wersja.
"@
    }
}

Write-Host ""
Write-Host "=========================================="
Write-Host "POLANIEONLINE UPDATE PACKAGE"
Write-Host "=========================================="
Write-Host "Nowa wersja:      $Version"

if ($NoDiff) {
    Write-Host "Diff:              WYLACZONY (pierwsze wydanie updatera)"
} else {
    Write-Host "Poprzednia wersja: $OldVersion"
    Write-Host "Diff:              $OldVersion -> $Version"
}

Write-Host ""

# Jedna sesja Anta jest wazna: client_build wykonuje sie raz.
$AntTargets = @(
    "clean",
    "dist_client_binary"
)

if (-not $NoDiff) {
    $AntTargets += "jardiff"
}

$AntTargets += "compile_polanieonlinetools"

$AntSucceeded = $false

try {
    Invoke-Checked $Ant $AntTargets
    $AntSucceeded = $true
}
catch {
    if (-not $NoDiff) {
        Restore-BuildArchiveAfterJarDiff $HistoryRaw
    }
    throw
}

$ClientJar = Join-Path $Root ("build\lib\polanieonline-" + $Version + ".jar")

Assert-JarVersion $Javap $ClientJar "games.stendhal.common.Debug" $Version
Assert-JarVersion $Javap $ClientJar "games.stendhal.common.Version" $Version

$FullFiles = @(
    [pscustomobject]@{ Name = "log4j.jar"; Source = (Join-Path $Root "libs\log4j.jar") },
    [pscustomobject]@{ Name = "jorbis.jar"; Source = (Join-Path $Root "libs\jorbis.jar") },
    [pscustomobject]@{ Name = "marauroa.jar"; Source = (Join-Path $Root "libs\marauroa.jar") },
    [pscustomobject]@{ Name = "json-simple-1.1.1.jar"; Source = (Join-Path $Root "libs\json-simple-1.1.1.jar") },
    [pscustomobject]@{ Name = "polanieonline-$Version.jar"; Source = (Join-Path $Root "build\lib\polanieonline-$Version.jar") },
    [pscustomobject]@{ Name = "polanieonline-data-$Version.jar"; Source = (Join-Path $Root "build\lib\polanieonline-data-$Version.jar") },
    [pscustomobject]@{ Name = "polanieonline-sound-data-$Version.jar"; Source = (Join-Path $Root "build\lib\polanieonline-sound-data-$Version.jar") },
    [pscustomobject]@{ Name = "polanieonline-music-data-$Version.jar"; Source = (Join-Path $Root "build\lib\polanieonline-music-data-$Version.jar") }
)

$DiffFiles = @()

if (-not $NoDiff) {
    $DiffFiles = @(
        [pscustomobject]@{
            Name = "polanieonline-diff-$OldVersion-$Version.jar"
            Source = (Join-Path $Root "build\lib\polanieonline-diff-$OldVersion-$Version.jar")
        },
        [pscustomobject]@{
            Name = "polanieonline-data-diff-$OldVersion-$Version.jar"
            Source = (Join-Path $Root "build\lib\polanieonline-data-diff-$OldVersion-$Version.jar")
        },
        [pscustomobject]@{
            Name = "polanieonline-sound-data-diff-$OldVersion-$Version.jar"
            Source = (Join-Path $Root "build\lib\polanieonline-sound-data-diff-$OldVersion-$Version.jar")
        },
        [pscustomobject]@{
            Name = "polanieonline-music-data-diff-$OldVersion-$Version.jar"
            Source = (Join-Path $Root "build\lib\polanieonline-music-data-diff-$OldVersion-$Version.jar")
        }
    )
}

foreach ($item in @($FullFiles + $DiffFiles)) {
    if (-not (Test-Path -LiteralPath $item.Source)) {
        if (-not $NoDiff) {
            Restore-BuildArchiveAfterJarDiff $HistoryRaw
        }
        throw "Brakuje pliku po buildzie: $($item.Source)"
    }
}

# Jesli biblioteka zmienila sie od poprzedniego oficjalnego ZIP-a,
# updater musi pobrac jej pelny nowy plik (jardiff ich nie obsluguje).
$ChangedLibraries = @()

if (-not $NoDiff) {
    $libraryChecks = @(
        [pscustomobject]@{
            Name = "log4j.jar"
            Current = (Join-Path $Root "libs\log4j.jar")
            Old = (Join-Path $BuildArchiveDir "lib\log4j.jar")
        },
        [pscustomobject]@{
            Name = "jorbis.jar"
            Current = (Join-Path $Root "libs\jorbis.jar")
            Old = (Join-Path $BuildArchiveDir "lib\jorbis.jar")
        },
        [pscustomobject]@{
            Name = "marauroa.jar"
            Current = (Join-Path $Root "libs\marauroa.jar")
            Old = (Join-Path $BuildArchiveDir "lib\marauroa.jar")
        },
        [pscustomobject]@{
            Name = "json-simple-1.1.1.jar"
            Current = (Join-Path $Root "libs\json-simple-1.1.1.jar")
            Old = (Join-Path $BuildArchiveDir "lib\json-simple-1.1.1.jar")
        }
    )

    foreach ($lib in $libraryChecks) {
        if (-not (Test-Path -LiteralPath $lib.Old)) {
            Restore-BuildArchiveAfterJarDiff $HistoryRaw
            throw "W starym oficjalnym ZIP-ie brakuje $($lib.Old)."
        }

        if ((Get-FileSha256 $lib.Current) -ne (Get-FileSha256 $lib.Old)) {
            $ChangedLibraries += $lib.Name
            Write-Host "[INFO] Biblioteka zmieniona: $($lib.Name) - zostanie pobrana w calosci."
        }
    }

    Restore-BuildArchiveAfterJarDiff $HistoryRaw
}

if (Test-Path -LiteralPath $PackageRoot) {
    Remove-Item -LiteralPath $PackageRoot -Recurse -Force
}

New-Item -ItemType Directory -Path $UploadDir -Force | Out-Null
New-Item -ItemType Directory -Path $ArchiveOutDir -Force | Out-Null

foreach ($item in @($FullFiles + $DiffFiles)) {
    Copy-Item -LiteralPath $item.Source -Destination (Join-Path $UploadDir $item.Name) -Force
}

$CurrentZip = Join-Path $Root ("build\polanieonline-" + $Version + ".zip")
if (-not (Test-Path -LiteralPath $CurrentZip)) {
    throw "Brakuje archiwum klienta: $CurrentZip"
}

Copy-Item -LiteralPath $CurrentZip -Destination (Join-Path $ArchiveOutDir ("polanieonline-" + $Version + ".zip")) -Force

# Zbuduj kompletna historie manifestu.
$Properties = [ordered]@{}

if (-not $NoDiff) {
    foreach ($key in $HistoryProperties.Keys) {
        if ($key -match '^(version\.(?!destination\.)|version\.destination\.|update-file-list\.|file-size\.|file-signature\.)') {
            $Properties[$key] = $HistoryProperties[$key]
        }
    }

    # Poprzedni CURRENT staje sie UPDATE_NEEDED.
    foreach ($key in @($Properties.Keys)) {
        if (($key -match '^version\.(?!destination\.)') -and ($Properties[$key] -eq "CURRENT")) {
            $Properties[$key] = "UPDATE_NEEDED"
        }
    }

    $Properties["version.$OldVersion"] = "UPDATE_NEEDED"
}

$Properties["version.$Version"] = "CURRENT"

$InitNames = @($FullFiles | ForEach-Object { $_.Name })
$Properties["init.version"] = $Version
$Properties["init.file-list"] = ($InitNames -join ",")

if (-not $NoDiff) {
    $UpdateNames = @()
    $UpdateNames += $ChangedLibraries
    $UpdateNames += @($DiffFiles | ForEach-Object { $_.Name })

    $Properties["version.destination.$OldVersion"] = $Version
    $Properties["update-file-list.$OldVersion"] = ($UpdateNames -join ",")
}

$FilesToSign = @($FullFiles + $DiffFiles)

foreach ($item in $FilesToSign) {
    $staged = Join-Path $UploadDir $item.Name
    $Properties["file-size.$($item.Name)"] = (Get-Item -LiteralPath $staged).Length.ToString()
}

$SignerClasspath = ".;build\build_polanieonlinetools;libs\ant.jar"
$SignArguments = @(
    "-cp",
    $SignerClasspath,
    "games.stendhal.tools.updateprop.UpdateSigner"
)

$SignArguments += @(
    $FilesToSign |
    ForEach-Object { (Join-Path $UploadDir $_.Name) }
)

Write-Host ""
Write-Host ">> Generowanie file-signature.*"
Write-Host ""

$SignerOutputRaw = & $Java @SignArguments 2>&1
$SignerExitCode = $LASTEXITCODE
$SignerOutput = @($SignerOutputRaw | ForEach-Object { "$_" })

$SignerOutput | ForEach-Object { Write-Host $_ }

if ($SignerExitCode -ne 0) {
    throw "UpdateSigner zakonczyl sie kodem $SignerExitCode."
}

$SignatureCount = 0

foreach ($line in $SignerOutput) {
    if ($line -match '^file-signature\.([^=]+)=(.+)$') {
        $fileName = $Matches[1]
        $signature = $Matches[2].Trim()
        $Properties["file-signature.$fileName"] = $signature
        $SignatureCount++
    }
}

if ($SignatureCount -ne $FilesToSign.Count) {
    throw "UpdateSigner wygenerowal $SignatureCount podpisow, oczekiwano $($FilesToSign.Count)."
}

foreach ($item in $FilesToSign) {
    $key = "file-signature.$($item.Name)"
    if (-not $Properties.Contains($key)) {
        throw "Brakuje podpisu dla $($item.Name)."
    }
}

function Get-KeysMatching([System.Collections.IDictionary]$Map, [string]$Pattern) {
    return @($Map.Keys | Where-Object { $_ -match $Pattern })
}

$ManifestLines = @()
$ManifestLines += "# PolanieOnLine automatic updater"
$ManifestLines += "# Generated by build-update-package.ps1 - do not edit sizes/signatures by hand"
$ManifestLines += ""

$ManifestLines += "# Version status"
foreach ($key in (Get-KeysMatching $Properties '^version\.(?!destination\.)')) {
    $ManifestLines += "$key=$($Properties[$key])"
}

$ManifestLines += ""
$ManifestLines += "# Update path"
foreach ($key in (Get-KeysMatching $Properties '^version\.destination\.')) {
    $ManifestLines += "$key=$($Properties[$key])"
}

$ManifestLines += ""
$ManifestLines += "# Fresh install"
$ManifestLines += "init.version=$($Properties['init.version'])"
$ManifestLines += "init.file-list=$($Properties['init.file-list'])"

$updateListKeys = Get-KeysMatching $Properties '^update-file-list\.'
if ($updateListKeys.Count -gt 0) {
    $ManifestLines += ""
    $ManifestLines += "# Incremental updates"
    foreach ($key in $updateListKeys) {
        $ManifestLines += "$key=$($Properties[$key])"
    }
}

$ManifestLines += ""
$ManifestLines += "# Exact file sizes"
foreach ($key in (Get-KeysMatching $Properties '^file-size\.')) {
    $ManifestLines += "$key=$($Properties[$key])"
}

$ManifestLines += ""
$ManifestLines += "# SHA1withRSA update signatures"
foreach ($key in (Get-KeysMatching $Properties '^file-signature\.')) {
    $ManifestLines += "$key=$($Properties[$key])"
}

$ManifestLines += ""

$ManifestPath = Join-Path $UploadDir "update-1.16.properties"
Write-AsciiLines $ManifestPath $ManifestLines

# Kopia manifestu razem z ZIP-em - to dokladnie ten zestaw, ktory po testach
# mozna wypromowac bez ponownego builda.
Copy-Item -LiteralPath $ManifestPath -Destination (Join-Path $ArchiveOutDir "update-1.16.properties") -Force

$HashLines = @()

Get-ChildItem -LiteralPath $UploadDir -File |
    Where-Object { $_.Name -ne "SHA256SUMS.txt" } |
    Sort-Object Name |
    ForEach-Object {
        $hash = Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256
        $HashLines += "$($hash.Hash)  $($_.Name)"
    }

Write-AsciiLines (Join-Path $UploadDir "SHA256SUMS.txt") $HashLines

Write-Host ""
Write-Host "=========================================="
Write-Host "PACZKA GOTOWA"
Write-Host "=========================================="
Write-Host "Wersja: $Version"

if (-not $NoDiff) {
    Write-Host "Diff:   $OldVersion -> $Version"

    if ($ChangedLibraries.Count -gt 0) {
        Write-Host ("Pelne zmienione biblioteki w update: " + ($ChangedLibraries -join ", "))
    }
}

Write-Host ""
Write-Host "Do wrzucenia na serwer:"
Write-Host "  $UploadDir"
Write-Host ""
Write-Host "Baza tej wersji do przyszlych diffow:"
Write-Host "  $ArchiveOutDir"
Write-Host ""
Write-Host "Po przetestowaniu TEGO DOKLADNEGO builda uruchom:"
Write-Host "  .\build-update-package.ps1 -PromoteOnly"
Write-Host ""
Write-Host "Przy kolejnych wydaniach nie usuwaj z serwera starych diffow,"
Write-Host "jesli chcesz obslugiwac graczy pomijajacych kilka wersji."
