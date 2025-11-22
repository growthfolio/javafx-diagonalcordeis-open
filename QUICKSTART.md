# 🚀 Quick Start - Criar Instalador Windows

## 3 Passos Simples

### 1️⃣ Abrir Terminal no Projeto
```batch
cd C:\caminho\para\diagonal-cordeis
```

### 2️⃣ Executar Script
```batch
build-installer.bat
```

### 3️⃣ Pegar o Instalador
```
📁 target\installer\Diagonal Cordeis-1.0.0.exe
```

---

## O que acontece:

```
[1/4] Limpando builds anteriores...     ✓
[2/4] Compilando aplicação...           ✓ (Maven)
[3/4] Verificando jpackage...           ✓
[4/4] Criando instalador Windows...     ✓

✅ BUILD CONCLUÍDO!

Instalador: target\installer\Diagonal Cordeis-1.0.0.exe
Banco de dados: %APPDATA%\DiagonalCordeis\data\diagonalcordeis.db
```

---

## Onde vão os arquivos:

### No Cliente (após instalação):

```
📂 C:\Program Files\DiagonalCordeis\     ← Aplicação
   ├── Diagonal Cordeis.exe
   └── runtime\                          ← Java incluído

📂 C:\Users\Usuario\AppData\Roaming\     ← Dados do usuário
   └── DiagonalCordeis\
       └── data\
           └── diagonalcordeis.db        ← Banco SQLite
```

---

## Testar o Instalador

```batch
# 1. Executar instalador
target\installer\Diagonal Cordeis-1.0.0.exe

# 2. Seguir wizard de instalação
Next > Next > Install > Finish

# 3. Executar aplicação
Menu Iniciar → Diagonal Cordéis

# 4. Verificar banco criado
Windows + R → %APPDATA%\DiagonalCordeis\data
```

---

## Problemas Comuns

### ❌ "Java não encontrado"
```batch
# Instale JDK 21
https://adoptium.net/
```

### ❌ "jpackage não encontrado"
```batch
# Verifique versão
java -version    # Deve ser 21 ou superior
jpackage --version
```

---

## Método Alternativo (sem jpackage)

Se `jpackage` não funcionar:

```batch
# 1. Compile
mvnw.cmd clean package -DskipTests

# 2. Distribua via ZIP
# Incluir:
# - target\cordeis-0.0.1-SNAPSHOT.jar
# - Script run.bat (ver abaixo)
# - LEIA-ME-INSTALACAO.txt
```

**run.bat:**
```batch
@echo off
java -jar cordeis-0.0.1-SNAPSHOT.jar
pause
```

---

## Documentação Completa

📖 **docs/GUIA_INSTALADOR_WINDOWS.md** - Guia técnico completo  
📖 **docs/RESUMO_INSTALADOR.md** - Resumo executivo  
📄 **LEIA-ME-INSTALACAO.txt** - Para o cliente

---

## Suporte

- Código: `src/main/java/com/diagonal/cordeis/config/DataSourceConfig.java`
- Issues: GitHub
- Email: suporte@diagonal.com.br

---

**Pronto! Seu instalador profissional está a um comando de distância! 🎉**
