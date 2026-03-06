param(
  [ValidateSet("unit", "manual", "main")]
  [string]$target = "unit"
)

$ErrorActionPreference = "Stop"

$projRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projRoot

$mainOut = "build/classes/main"
$testOut = "build/classes/test"

New-Item -ItemType Directory -Force -Path $mainOut, $testOut | Out-Null

Write-Host "Compiling (Java 17)..." -ForegroundColor Cyan

$mainSources = Get-ChildItem -Recurse -Filter *.java "src/main/java" | ForEach-Object { $_.FullName }
if (-not $mainSources -or $mainSources.Count -eq 0) { throw "No main Java sources found under src/main/java" }
javac --release 17 -d $mainOut $mainSources

$testSources = Get-ChildItem -Recurse -Filter *.java "src/test/java" | ForEach-Object { $_.FullName }
if ($testSources -and $testSources.Count -gt 0) {
  javac --release 17 -cp $mainOut -d $testOut $testSources
}

if ($target -eq "main") {
  Write-Host "Running Main..." -ForegroundColor Green
  java -cp "$mainOut" com.csd201.dungeon.Main
} elseif ($target -eq "manual") {
  Write-Host "Running InventoryListManualTest..." -ForegroundColor Green
  java -cp "$testOut;$mainOut" com.csd201.dungeon.InventoryListManualTest
} else {
  Write-Host "Running unit tests..." -ForegroundColor Green
  java -ea -cp "$testOut;$mainOut" com.csd201.dungeon.unit.UnitTestRunner
}

