@echo off
REM ========================================
REM Build Instalador com Inno Setup
REM Método alternativo (não precisa de módulos JavaFX)
REM ========================================

echo.
echo ========================================
echo  Diagonal Cordeis - Build com Inno Setup
echo ========================================
echo.

echo [1/3] Compilando aplicacao...
call mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERRO: Falha ao compilar a aplicacao!
    pause
    exit /b 1
)

echo.
echo [2/3] Criando estrutura para instalador...
if not exist target\installer-files mkdir target\installer-files
copy target\cordeis-0.0.1-SNAPSHOT.jar target\installer-files\
copy init-db.sql target\installer-files\ 2>nul

REM Criar script de execução
echo @echo off > target\installer-files\DiagonalCordeis.bat
echo start javaw -jar cordeis-0.0.1-SNAPSHOT.jar >> target\installer-files\DiagonalCordeis.bat

echo.
echo [3/3] Verificando Inno Setup...
where iscc >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo AVISO: Inno Setup nao encontrado!
    echo.
    echo Para criar o instalador:
    echo 1. Instale o Inno Setup: https://jrsoftware.org/isdl.php
    echo 2. Execute: iscc installer-script.iss
    echo.
    echo Arquivos preparados em: target\installer-files\
    pause
    exit /b 0
)

echo Criando instalador com Inno Setup...
iscc installer-script.iss

if %errorlevel% eq 0 (
    echo.
    echo ========================================
    echo  BUILD CONCLUIDO!
    echo ========================================
    echo.
    dir /b target\installer\*.exe 2>nul
)

pause
