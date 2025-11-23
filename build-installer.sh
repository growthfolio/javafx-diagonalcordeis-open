#!/bin/bash
# ========================================
# Build Instalador Windows - Do Linux
# ========================================

echo ""
echo "========================================"
echo "  Diagonal Cordeis - Build para Windows"
echo "  (Executando no Linux)"
echo "========================================"
echo ""

# Verificar se Java está instalado
if ! command -v java &> /dev/null; then
    echo "❌ ERRO: Java não encontrado! Instale o JDK 21."
    exit 1
fi

echo "[1/3] Limpando builds anteriores..."
rm -rf target/installer
./mvnw clean

echo ""
echo "[2/3] Compilando aplicação..."
./mvnw package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ ERRO: Falha ao compilar a aplicação!"
    exit 1
fi

echo ""
echo "[3/3] Preparando para distribuição..."
echo ""
echo "⚠️  IMPORTANTE: jpackage não cria instaladores Windows no Linux!"
echo ""
echo "📦 Opções para distribuir ao cliente Windows:"
echo ""
echo "1️⃣  MAIS SIMPLES - ZIP com JAR executável:"
echo "   - Distribua: target/cordeis-0.0.1-SNAPSHOT.jar"
echo "   - Cliente precisa ter Java 21 instalado"
echo ""
echo "2️⃣  COM INSTALADOR - Usar Windows/VM:"
echo "   - Compile em máquina Windows (ou VM)"
echo "   - Execute: build-installer.bat"
echo ""
echo "3️⃣  DOCKER - Build em container Windows (avançado)"
echo ""

# Criar pacote ZIP para distribuição simples
echo "Criando pacote ZIP para distribuição..."
mkdir -p target/dist
cp target/cordeis-0.0.1-SNAPSHOT.jar target/dist/

# Criar script Windows para execução robusto com verificação de Java
cat > target/dist/DiagonalCordeis.bat << 'WINSCRIPT'
@echo off
title Diagonal Cordeis

REM Mudar para o diretorio da aplicacao
cd /d "%~dp0"

REM Verificar se Java esta instalado
where javaw >nul 2>nul
if %errorlevel% neq 0 (
    echo.
    echo ========================================
    echo   ERRO: Java nao encontrado!
    echo ========================================
    echo.
    echo Este programa requer Java 21 ou superior.
    echo.
    echo Por favor, instale o Java de:
    echo https://adoptium.net/temurin/releases/?version=21
    echo.
    echo Pressione qualquer tecla para fechar...
    pause >nul
    exit /b 1
)

REM Iniciar a aplicacao
echo Iniciando Diagonal Cordeis...
javaw -Xms256m -Xmx1024m -Dfile.encoding=UTF-8 -jar "%~dp0cordeis-0.0.1-SNAPSHOT.jar"

REM Verificar se houve erro ao iniciar
if %errorlevel% neq 0 (
    echo.
    echo ========================================
    echo   ERRO: Falha ao iniciar aplicacao
    echo ========================================
    echo.
    echo Codigo de erro: %errorlevel%
    echo.
    echo Possiveis causas:
    echo - Versao do Java incompativel (necessario Java 21+)
    echo - Arquivo JAR corrompido ou ausente
    echo - Falta de memoria
    echo.
    echo Pressione qualquer tecla para fechar...
    pause >nul
    exit /b %errorlevel%
)
WINSCRIPT

# Criar README para cliente
cat > target/dist/LEIA-ME.txt << 'README'
================================================================================
                        DIAGONAL CORDÉIS
================================================================================

REQUISITOS:
-----------
- Windows 10 ou superior
- Java 21 instalado
  Download: https://adoptium.net/

INSTALAÇÃO:
-----------
1. Instale o Java 21 (se ainda não tiver)
2. Extraia todos os arquivos desta pasta
3. Duplo clique em: DiagonalCordeis.bat

LOCALIZAÇÃO DOS DADOS:
----------------------
O banco de dados será criado automaticamente em:
%APPDATA%\DiagonalCordeis\data\diagonalcordeis.db

Exemplo: C:\Users\SeuNome\AppData\Roaming\DiagonalCordeis\data\

PRIMEIRO ACESSO:
----------------
Usuário: admin
Senha: admin123

SUPORTE:
--------
suporte@diagonal.com.br

================================================================================
README

cp LEIA-ME-INSTALACAO.txt target/dist/ 2>/dev/null || true

# Criar ZIP
cd target/dist
zip -q DiagonalCordeis-1.0.0.zip cordeis-0.0.1-SNAPSHOT.jar DiagonalCordeis.bat LEIA-ME.txt LEIA-ME-INSTALACAO.txt 2>/dev/null
cd ../..

echo ""
echo "========================================"
echo "  ✅ BUILD CONCLUÍDO!"
echo "========================================"
echo ""
echo "📦 Pacote criado:"
echo "   target/dist/DiagonalCordeis-1.0.0.zip"
echo ""
echo "📁 Conteúdo:"
echo "   ✓ cordeis-0.0.1-SNAPSHOT.jar"
echo "   ✓ DiagonalCordeis.bat (script Windows)"
echo "   ✓ LEIA-ME.txt"
echo ""
echo "🚀 Para distribuir:"
echo "   1. Envie o arquivo ZIP ao cliente"
echo "   2. Cliente extrai e executa DiagonalCordeis.bat"
echo "   3. Cliente precisa ter Java 21 instalado"
echo ""
echo "💡 Para instalador .EXE profissional:"
echo "   - Use Windows ou VM Windows"
echo "   - Execute: build-installer.bat"
echo ""
