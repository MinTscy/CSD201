@echo off
setlocal
set PORT=%1
if "%PORT%"=="" set PORT=8081
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-web.ps1" -port %PORT%
endlocal
