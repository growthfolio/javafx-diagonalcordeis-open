@echo off
REM ========================================
REM Testar caminho do banco de dados
REM ========================================

echo.
echo ========================================
echo  Teste de Caminho do Banco de Dados
echo ========================================
echo.

echo Compilando aplicacao...
call mvnw.cmd clean package -DskipTests -q

if %errorlevel% neq 0 (
    echo ERRO ao compilar!
    pause
    exit /b 1
)

echo.
echo Executando aplicacao (modo teste)...
echo O caminho do banco sera exibido nos logs.
echo.
echo Pressione Ctrl+C para sair quando a janela abrir.
echo.
pause

java -jar target\cordeis-0.0.1-SNAPSHOT.jar

pause
