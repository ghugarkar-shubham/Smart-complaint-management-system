param(
  [Parameter(ValueFromRemainingArguments = $true)]
  [string[]]$MavenArgs
)

$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$mavenVersion = '3.9.11'
$mavenHome = Join-Path $scriptDir ".mvn/apache-maven-$mavenVersion"
$downloadUrl = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
$archivePath = Join-Path $env:TEMP "apache-maven-$mavenVersion-bin.zip"

if (-not (Test-Path $mavenHome)) {
  Write-Host "Downloading Maven $mavenVersion..."
  Invoke-WebRequest -Uri $downloadUrl -OutFile $archivePath
  New-Item -ItemType Directory -Force -Path (Join-Path $scriptDir '.mvn') | Out-Null
  Expand-Archive -Path $archivePath -DestinationPath (Join-Path $scriptDir '.mvn') -Force
}

$mvnCommand = Join-Path $mavenHome 'bin/mvn.cmd'
if (-not (Test-Path $mvnCommand)) {
  throw "Maven executable not found at $mvnCommand"
}

& $mvnCommand @MavenArgs
exit $LASTEXITCODE
