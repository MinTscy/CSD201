param(
  [int]$port = 8081
)

$ErrorActionPreference = "Stop"

$projRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projRoot

$mvn = Join-Path $projRoot ".tools\apache-maven-3.9.9\bin\mvn.cmd"
if (-not (Test-Path $mvn)) {
  throw "Maven not found at $mvn"
}

Write-Host "Starting web UI at http://localhost:$port" -ForegroundColor Green
& $mvn "-Dexec.args=$port" exec:java
