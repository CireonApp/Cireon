Param(
    [ValidateSet("app-image", "exe")]
    [string]$Type = "app-image",

    [string]$Name = "CireonBackend",
    [string]$MainJar = "server-0.0.1-SNAPSHOT.jar",
    [string]$MainClass = "org.springframework.boot.loader.launch.JarLauncher",

    [string]$InputDir = (Join-Path $PSScriptRoot "target"),
    [string]$DestDir = (Join-Path $PSScriptRoot "dist"),

    [switch]$SkipBuild
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Require-Command {
    Param([Parameter(Mandatory = $true)][string]$Name)

    $cmd = Get-Command $Name -ErrorAction SilentlyContinue
    if (-not $cmd) {
        throw "Required command '$Name' is not available on PATH."
    }
}

function Resolve-LocalOrPathCommand {
    Param(
        [Parameter(Mandatory = $true)][string]$LocalPath,
        [Parameter(Mandatory = $true)][string]$CommandName
    )

    if (Test-Path -Path $LocalPath -PathType Leaf) {
        return $LocalPath
    }

    $cmd = Get-Command $CommandName -ErrorAction SilentlyContinue
    if ($cmd) {
        return $cmd.Source
    }

    throw "Required command '$CommandName' not found. Checked local path '$LocalPath' and PATH."
}

function Test-WixInstalled {
    $candle = Get-Command candle -ErrorAction SilentlyContinue
    $light = Get-Command light -ErrorAction SilentlyContinue
    return ($null -ne $candle -and $null -ne $light)
}

Write-Host "==> Packaging type: $Type"

Require-Command -Name "jpackage"

if (-not $SkipBuild) {
    $mavenWrapperPath = Resolve-LocalOrPathCommand -LocalPath (Join-Path $PSScriptRoot "mvnw.cmd") -CommandName "mvnw.cmd"
    Write-Host "==> Building project with Maven"
    & $mavenWrapperPath -q clean package
}

if (-not (Test-Path -Path $InputDir -PathType Container)) {
    throw "Input directory not found: $InputDir"
}

$jarPath = Join-Path $InputDir $MainJar
if (-not (Test-Path -Path $jarPath -PathType Leaf)) {
    throw "Main JAR not found: $jarPath"
}

if ($Type -eq "exe" -and -not (Test-WixInstalled)) {
    throw "WiX tools were not found (candle.exe and light.exe). Install WiX v3+ and add it to PATH, or use -Type app-image."
}

if (-not (Test-Path -Path $DestDir -PathType Container)) {
    New-Item -ItemType Directory -Path $DestDir | Out-Null
}

if ($Type -eq "app-image") {
    $existingImageDir = Join-Path $DestDir $Name
    if (Test-Path -Path $existingImageDir -PathType Container) {
        Write-Host "==> Removing existing app image: $existingImageDir"
        Remove-Item -Path $existingImageDir -Recurse -Force
    }
}

$jpackageArgs = @(
    "--type", $Type,
    "--name", $Name,
    "--input", $InputDir,
    "--main-jar", $MainJar,
    "--main-class", $MainClass,
    "--win-console",
    "--dest", $DestDir
)

Write-Host "==> Running jpackage"
& jpackage @jpackageArgs

if ($LASTEXITCODE -ne 0) {
    throw "jpackage failed with exit code $LASTEXITCODE"
}

if ($Type -eq "app-image") {
    $exePath = Join-Path (Join-Path $DestDir $Name) ("$Name.exe")
    Write-Host "==> Done. App image launcher: $exePath"
} else {
    Write-Host "==> Done. Installer output folder: $DestDir"
}

