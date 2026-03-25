param(
  [ValidateSet("unit", "manual", "main")]
  [string]$target = "unit",
  [int]$port = 8080
)

$ErrorActionPreference = "Stop"

$projRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projRoot

$mvn = Join-Path $projRoot ".tools\apache-maven-3.9.9\bin\mvn.cmd"
if (-not (Test-Path $mvn)) { throw "Maven not found at $mvn" }

switch ($target) {
  "unit" {
    Write-Host "Running JUnit test suite via Maven..." -ForegroundColor Green
    & $mvn test
  }
  "manual" {
    Write-Host "Compiling (skipping tests)..." -ForegroundColor Cyan
    & $mvn -q -DskipTests compile
    Write-Host "Running InventoryListManualTest..." -ForegroundColor Green
    java -cp "target/test-classes;target/classes" com.csd201.dungeon.InventoryListManualTest
  }
  "main" {
    Write-Host "Starting Main (web UI) on port $port..." -ForegroundColor Green
    & $mvn "-Dexec.args=$port" exec:java
  }
}
