@echo off
setlocal

REM Usage:
REM   run.cmd            -> compile + run unit tests
REM   run.cmd unit       -> compile + run unit tests
REM   run.cmd manual     -> compile + run manual test
REM   run.cmd main       -> compile + run Main demo

set TARGET=%1
if "%TARGET%"=="" set TARGET=unit

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run.ps1" -target "%TARGET%"
endlocal

