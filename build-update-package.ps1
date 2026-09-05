# build-update-package.ps1 v6 - fix reserved HOME variable collision + robust JDK/Ant discovery
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

function Read-AntWrapperEnvironment {
    $wrapper = Join-Path $Root "ant.cmd"
    $antHome = $null
    $javaHome = $null

    if (Test-Path -LiteralPath $wrapper) {
        foreach ($line in Get-Content -LiteralPath $wrapper) {
            if ($line -match '^\s*set\s+ANT_HOME=(.+?)\s*$') {
                $antHome = $Matches[1].Trim().Trim('"')
                continue
            }

            if ($line -match '^\s*set\s+JAVA_HOME=(.+?)\s*$') {
                $javaHome = $Matches[1].Trim().Trim('"')
            }
        }
    }

    [pscustomobject]@{
        AntHome = $antHome
        JavaHome = $javaHome
    }
}

function Test-JavaHome([string]$Path) {
    if ([string]::IsNullOrWhiteSpace($Path)) {
        return $false
    }

    return (Test-Path -LiteralPath (Join-Path $Path "bin\java.exe")) -and
        (Test-Path -LiteralPath (Join-Path $Path "bin\javap.exe"))
}

function Resolve-JavaHome([string]$WrapperJavaHome) {
    $directCandidates = @()

    if (-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
        $directCandidates += $env:JAVA_HOME
    }

    if (-not [string]::IsNullOrWhiteSpace($WrapperJavaHome)) {
        $directCandidates += $WrapperJavaHome
    }

    foreach ($candidate in ($directCandidates | Select-Object -Unique)) {
        if (Test-JavaHome $candidate) {
            return (Resolve-Path -LiteralPath $candidate).Path
        }
    }

    # ant.cmd bywa stary (np. wskazuje Z:\Java\jdk, gdy faktyczny JDK to
    # Z:\Java\jdk-11). Jesli wskazany katalog nie istnieje, sprawdz rodzenstwo
    # jdk* w tym samym katalogu nadrzednym. Preferujemy JDK 11, bo na nim jest
    # obecnie budowany release, a potem pozostale dostepne JDK.
    $parents = @()
    foreach ($candidate in ($directCandidates | Select-Object -Unique)) {
        try {
            $parent = Split-Path -Parent $candidate
            if (-not [string]::IsNullOrWhiteSpace($parent)) {
                $parents += $parent
            }
        } catch {
            # Niepoprawna sciezka kandydata - przechodzimy do kolejnego zrodla.
        }
    }

    foreach ($parent in ($parents | Select-Object -Unique)) {
        if (-not (Test-Path -LiteralPath $parent)) {
            continue
        }

        $allJdkDirs = @(
            Get-ChildItem -LiteralPath $parent -Directory -ErrorAction SilentlyContinue |
                Where-Object { $_.Name -like "jdk*" }
        )
        $jdk11Dirs = @(
            $allJdkDirs |
                Where-Object { $_.Name -match '^jdk-?11(?:[._-]|$)' } |
                Sort-Object Name -Descending
        )
        $otherJdkDirs = @(
            $allJdkDirs |
                Where-Object { $_.Name -notmatch '^jdk-?11(?:[._-]|$)' } |
                Sort-Object Name -Descending
        )
        $jdkDirs = @($jdk11Dirs + $otherJdkDirs)

        foreach ($jdkDir in $jdkDirs) {
            if (Test-JavaHome $jdkDir.FullName) {
                Write-Host "[INFO] JAVA_HOME z ant.cmd nie wskazuje pelnego JDK; uzywam $($jdkDir.FullName)"
                return $jdkDir.FullName
            }
        }
    }

    $javap = Get-Command "javap.exe" -ErrorAction SilentlyContinue
    if (-not $javap) {
        $javap = Get-Command "javap" -ErrorAction SilentlyContinue
    }

    if ($javap) {
        $binDir = Split-Path -Parent $javap.Source
        $resolvedJavaHome = Split-Path -Parent $binDir
        if (Test-JavaHome $resolvedJavaHome) {
            return $resolvedJavaHome
        }
    }

    $shown = @($directCandidates | Select-Object -Unique) -join ", "
    throw "Nie znaleziono pelnego JDK (java.exe + javap.exe). Sprawdzone JAVA_HOME: $shown"
}

function Resolve-AntTool([string]$WrapperAntHome) {
    $candidates = @()

    if (-not [string]::IsNullOrWhiteSpace($env:ANT_HOME)) {
        $candidates += $env:ANT_HOME
    }

    if (-not [string]::IsNullOrWhiteSpace($WrapperAntHome)) {
        $candidates += $WrapperAntHome
    }

    foreach ($antHomeCandidate in ($candidates | Select-Object -Unique)) {
        $candidate = Join-Path $antHomeCandidate "bin\ant.bat"
        if (Test-Path -LiteralPath $candidate) {
            $env:ANT_HOME = $antHomeCandidate
            return $candidate
        }

        $candidate = Join-Path $antHomeCandidate "bin\ant"
        if (Test-Path -LiteralPath $candidate) {
            $env:ANT_HOME = $antHomeCandidate
            return $candidate
        }
    }

    $cmd = Get-Command "ant.bat" -ErrorAction SilentlyContinue
    if (-not $cmd) {
        $cmd = Get-Command "ant" -ErrorAction SilentlyContinue
    }

    if (-not $cmd) {
        throw "Nie znaleziono Anta. Sprawdz ANT_HOME w ant.cmd albo ustaw ANT_HOME."
    }

    return $cmd.Source
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
        throw "Nie znaleziono narzedzia '$Name'. JAVA_HOME=$env:JAVA_HOME"
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

# ant.cmd jest lokalnym wrapperem z wpisanym na sztywno "ant dist". Nie wolno
# uzywac go jako runnera targetow. Czytamy z niego tylko kandydatow ANT_HOME i
# JAVA_HOME, a nastepnie walidujemy je przed uzyciem.
$WrapperEnvironment = Read-AntWrapperEnvironment
$ResolvedJavaHome = Resolve-JavaHome $WrapperEnvironment.JavaHome
$env:JAVA_HOME = $ResolvedJavaHome

$Ant = Resolve-AntTool $WrapperEnvironment.AntHome
$Java = Resolve-JavaTool "java"
$Javap = Resolve-JavaTool "javap"

Write-Host "[INFO] ANT_HOME=$env:ANT_HOME"
Write-Host "[INFO] JAVA_HOME=$env:JAVA_HOME"
Write-Host "[INFO] Ant: $Ant"
Write-Host "[INFO] Java: $Java"
Write-Host "[INFO] Javap: $Javap"

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

# Nie wywolujemy ant.cmd: ten lokalny wrapper zawsze robi pelne "ant dist" i
# ignoruje przekazane targety. Odpalamy bezposrednio prawdziwy ant.bat.
# Nie wywolujemy tez targetu jardiff z Anta, bo zalezy od client_build i przy
# osobnym wywolaniu przebudowalby klienta drugi raz. Diffy powstaja nizej przez
# libs\jardiff.jar z TEGO SAMEGO buildu.
Invoke-Checked $Ant @("clean")
Invoke-Checked $Ant @("dist_client_binary")
Invoke-Checked $Ant @("compile_polanieonlinetools")

$ClientJar = Join-Path $Root ("build\lib\polanieonline-" + $Version + ".jar")

Assert-JarVersion $Javap $ClientJar "games.stendhal.common.Debug" $Version
Assert-JarVersion $Javap $ClientJar "games.stendhal.common.Version" $Version

$OldExtractDir = $null

if (-not $NoDiff) {
    $JarDiffTool = Join-Path $Root "libs\jardiff.jar"
    if (-not (Test-Path -LiteralPath $JarDiffTool)) {
        throw "Brakuje narzedzia jardiff: $JarDiffTool"
    }

    $OldExtractDir = Join-Path $Root ("build\update-base-" + $OldVersion)

    if (Test-Path -LiteralPath $OldExtractDir) {
        Remove-Item -LiteralPath $OldExtractDir -Recurse -Force
    }

    New-Item -ItemType Directory -Path $OldExtractDir -Force | Out-Null
    Expand-Archive -LiteralPath $OldZip -DestinationPath $OldExtractDir -Force

    $JarDiffJobs = @(
        [pscustomobject]@{
            Old = (Join-Path $OldExtractDir "lib\polanieonline.jar")
            New = (Join-Path $Root "build\lib\polanieonline-$Version.jar")
            Output = (Join-Path $Root "build\lib\polanieonline-diff-$OldVersion-$Version.jar")
        },
        [pscustomobject]@{
            Old = (Join-Path $OldExtractDir "lib\polanieonline-data.jar")
            New = (Join-Path $Root "build\lib\polanieonline-data-$Version.jar")
            Output = (Join-Path $Root "build\lib\polanieonline-data-diff-$OldVersion-$Version.jar")
        },
        [pscustomobject]@{
            Old = (Join-Path $OldExtractDir "lib\polanieonline-sound-data.jar")
            New = (Join-Path $Root "build\lib\polanieonline-sound-data-$Version.jar")
            Output = (Join-Path $Root "build\lib\polanieonline-sound-data-diff-$OldVersion-$Version.jar")
        },
        [pscustomobject]@{
            Old = (Join-Path $OldExtractDir "lib\polanieonline-music-data.jar")
            New = (Join-Path $Root "build\lib\polanieonline-music-data-$Version.jar")
            Output = (Join-Path $Root "build\lib\polanieonline-music-data-diff-$OldVersion-$Version.jar")
        }
    )

    foreach ($job in $JarDiffJobs) {
        if (-not (Test-Path -LiteralPath $job.Old)) {
            throw "W archiwum $OldZip brakuje: $($job.Old)"
        }
        if (-not (Test-Path -LiteralPath $job.New)) {
            throw "Brakuje nowego JAR-a: $($job.New)"
        }

        Invoke-Checked $Java @(
            "-jar",
            $JarDiffTool,
            "-nonminimal",
            "-creatediff",
            "-output",
            $job.Output,
            $job.Old,
            $job.New
        )
    }
}

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
            Old = (Join-Path $OldExtractDir "lib\log4j.jar")
        },
        [pscustomobject]@{
            Name = "jorbis.jar"
            Current = (Join-Path $Root "libs\jorbis.jar")
            Old = (Join-Path $OldExtractDir "lib\jorbis.jar")
        },
        [pscustomobject]@{
            Name = "marauroa.jar"
            Current = (Join-Path $Root "libs\marauroa.jar")
            Old = (Join-Path $OldExtractDir "lib\marauroa.jar")
        },
        [pscustomobject]@{
            Name = "json-simple-1.1.1.jar"
            Current = (Join-Path $Root "libs\json-simple-1.1.1.jar")
            Old = (Join-Path $OldExtractDir "lib\json-simple-1.1.1.jar")
        }
    )

    foreach ($lib in $libraryChecks) {
        if (-not (Test-Path -LiteralPath $lib.Old)) {
            throw "W starym oficjalnym ZIP-ie brakuje $($lib.Old)."
        }

        if ((Get-FileSha256 $lib.Current) -ne (Get-FileSha256 $lib.Old)) {
            $ChangedLibraries += $lib.Name
            Write-Host "[INFO] Biblioteka zmieniona: $($lib.Name) - zostanie pobrana w calosci."
        }
    }

    if ($OldExtractDir -and (Test-Path -LiteralPath $OldExtractDir)) {
        Remove-Item -LiteralPath $OldExtractDir -Recurse -Force
        $OldExtractDir = $null
    }
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
