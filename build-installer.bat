@echo off
REM ========================================
REM Build Instalador - Diagonal Cordéis
REM ========================================

echo.
echo ========================================
echo  Diagonal Cordeis - Build Instalador
echo ========================================
echo.

REM Verificar se Java está instalado
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERRO: Java nao encontrado! Instale o JDK 21.
    pause
    exit /b 1
)

echo [1/4] Limpando builds anteriores...
if exist target\installer rmdir /s /q target\installer
call mvnw.cmd clean

echo.
echo [2/4] Compilando aplicacao...
call mvnw.cmd package -DskipTests
if %errorlevel% neq 0 (
    echo ERRO: Falha ao compilar a aplicacao!
    pause
    exit /b 1
)

echo.
echo [3/4] Verificando jpackage...
jpackage --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERRO: jpackage nao encontrado! Certifique-se de usar JDK 21+
    pause
    exit /b 1
)

echo.
echo [4/4] Criando instalador Windows...

REM Criar diretório de saída
if not exist target\installer mkdir target\installer

REM Criar instalador EXE
jpackage ^
  --type exe ^
  --name "Diagonal Cordeis" ^
  --app-version 1.0.0 ^
  --vendor "Diagonal" ^
  --description "Sistema de gestao e impressao de cordeis" ^
  --input target ^
  --main-jar cordeis-0.0.1-SNAPSHOT.jar ^
  --main-class com.diagonal.cordeis.AppLauncher ^
  --java-options "-Xms256m" ^
  --java-options "-Xmx1024m" ^
  --java-options "-Dfile.encoding=UTF-8" ^
  --win-menu ^
  --win-dir-chooser ^
  --win-shortcut ^
  --win-menu-group "Diagonal Cordeis" ^
  --win-per-user-install ^
  --dest target/installer

if %errorlevel% neq 0 (
    echo ERRO: Falha ao criar instalador!
    pause
    exit /b 1
)

echo.
echo ========================================
echo  BUILD CONCLUIDO COM SUCESSO!
echo ========================================
echo.
echo Instalador criado em: target\installer\
dir /b target\installer\*.exe
echo.
echo O banco de dados sera criado automaticamente em:
echo %%APPDATA%%\DiagonalCordeis\data\diagonalcordeis.db
echo.
pause
