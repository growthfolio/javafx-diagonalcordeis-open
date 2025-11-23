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

REM Criar script de execução robusto com verificação de Java e tratamento de erros
(
echo @echo off
echo title Diagonal Cordeis
echo.
echo REM Mudar para o diretorio da aplicacao
echo cd /d "%%~dp0"
echo.
echo REM Verificar se Java esta instalado
echo where javaw ^>nul 2^>nul
echo if %%errorlevel%% neq 0 ^(
echo     echo.
echo     echo ========================================
echo     echo   ERRO: Java nao encontrado!
echo     echo ========================================
echo     echo.
echo     echo Este programa requer Java 21 ou superior.
echo     echo.
echo     echo Por favor, instale o Java de:
echo     echo https://adoptium.net/temurin/releases/?version=21
echo     echo.
echo     echo Pressione qualquer tecla para fechar...
echo     pause ^>nul
echo     exit /b 1
echo ^)
echo.
echo REM Iniciar a aplicacao
echo echo Iniciando Diagonal Cordeis...
echo javaw -Xms256m -Xmx1024m -Dfile.encoding=UTF-8 -jar "%%~dp0cordeis-0.0.1-SNAPSHOT.jar"
echo.
echo REM Verificar se houve erro ao iniciar
echo if %%errorlevel%% neq 0 ^(
echo     echo.
echo     echo ========================================
echo     echo   ERRO: Falha ao iniciar aplicacao
echo     echo ========================================
echo     echo.
echo     echo Codigo de erro: %%errorlevel%%
echo     echo.
echo     echo Possiveis causas:
echo     echo - Versao do Java incompativel ^(necessario Java 21+^)
echo     echo - Arquivo JAR corrompido ou ausente
echo     echo - Falta de memoria
echo     echo.
echo     echo Pressione qualquer tecla para fechar...
echo     pause ^>nul
echo     exit /b %%errorlevel%%
echo ^)
) > target\installer-files\DiagonalCordeis.bat

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
