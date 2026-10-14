@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"
title Planova - Iniciar
echo ==============================
echo   Planova - Iniciando aplicacion
echo ==============================
echo.
where docker >nul 2>&1
if errorlevel 1 (
  echo [ERROR] Docker no esta instalado. Instala Docker Desktop: https://www.docker.com/products/docker-desktop/
  pause
  exit /b 1
)
docker info >nul 2>&1
if not errorlevel 1 goto docker_ok
echo Docker no esta en ejecucion. Intentando iniciar Docker Desktop...
if exist "%ProgramFiles%\Docker\Docker\Docker Desktop.exe" (
  start "" "%ProgramFiles%\Docker\Docker\Docker Desktop.exe"
) else (
  echo [ERROR] No se encontro Docker Desktop. Inicialo manualmente y vuelve a ejecutar este archivo.
  pause
  exit /b 1
)
set /a intentos=0
:espera
docker info >nul 2>&1
if not errorlevel 1 goto docker_ok
set /a intentos+=1
if %intentos% GEQ 40 (
  echo [ERROR] Docker no arranco a tiempo. Inicia Docker Desktop manualmente e intenta de nuevo.
  pause
  exit /b 1
)
echo Esperando a Docker... (%intentos%/40)
"%SystemRoot%\System32\timeout.exe" /t 3 /nobreak >nul
goto espera
:docker_ok
if not exist ".env" (
  copy ".env.example" ".env" >nul
  echo Se creo el archivo .env a partir de .env.example. Puedes editarlo para cambiar puertos o contrasenas.
)
set "FRONTEND_PORT=5173"
for /f "usebackq tokens=1,* delims==" %%A in (".env") do if "%%A"=="FRONTEND_PORT" set "FRONTEND_PORT=%%B"
echo.
echo Construyendo e iniciando contenedores (la primera vez puede tardar varios minutos)...
docker compose up -d --build
if errorlevel 1 (
  echo.
  echo [ERROR] Ocurrio un problema al iniciar Planova. Revisa los mensajes anteriores.
  pause
  exit /b 1
)
echo.
echo Planova esta en ejecucion:
echo   Aplicacion: http://localhost:%FRONTEND_PORT%
echo   Para detenerla ejecuta DETENER.bat
echo.
echo Esperando a que el sistema este listo...
"%SystemRoot%\System32\timeout.exe" /t 10 /nobreak >nul
start "" http://localhost:%FRONTEND_PORT%
echo.
pause
