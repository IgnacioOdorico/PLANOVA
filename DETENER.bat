@echo off
chcp 65001 >nul
cd /d "%~dp0"
title Planova - Detener
echo Deteniendo Planova (los datos se conservan)...
docker compose down
if errorlevel 1 (
  echo [ERROR] No se pudo detener Planova. Verifica que Docker este en ejecucion.
) else (
  echo Planova se detuvo correctamente.
)
echo.
pause
