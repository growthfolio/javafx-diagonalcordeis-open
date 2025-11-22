# 🚀 Como Distribuir para Cliente Windows (Usando Linux)

## ⚡ Solução Rápida (AGORA)

### 1. Compile no Linux:
```bash
./build-installer.sh
```

### 2. Pegue o arquivo:
```bash
target/dist/DiagonalCordeis-1.0.0.zip
```

### 3. Envie ao cliente

**O cliente deve:**
1. Descompactar o ZIP
2. Instalar Java 21: https://adoptium.net/
3. Duplo clique em `DiagonalCordeis.bat`

✅ **Pronto!** A aplicação funciona normalmente.

---

## 📊 O que o ZIP contém:

```
DiagonalCordeis-1.0.0.zip (98 MB)
├── cordeis-0.0.1-SNAPSHOT.jar      (aplicação)
├── DiagonalCordeis.bat              (executável Windows)
└── LEIA-ME.txt                      (instruções)
```

---

## 🎯 Banco de Dados - Onde Fica?

**Automaticamente criado em:**
```
C:\Users\<cliente>\AppData\Roaming\DiagonalCordeis\data\diagonalcordeis.db
```

✅ Separado da aplicação (seguro)  
✅ Não é deletado se reinstalar  
✅ Fácil fazer backup (copiar arquivo .db)

---

## 💡 Para Instalador .EXE Profissional (Futuro)

Se quiser criar instalador que **não precisa Java no cliente**:

### Opção 1: Usar Windows Temporariamente
```bash
# Em máquina/VM Windows:
git clone <seu-repo>
cd diagonal-cordeis
build-installer.bat

# Resultado: DiagonalCordeis-Setup.exe (com JRE incluído)
```

### Opção 2: GitHub Actions (Recomendado)
Configure CI/CD para compilar automaticamente no Windows.

Ver: `docs/BUILD_NO_LINUX.md` (seção GitHub Actions)

---

## ❓ FAQ

### "Cliente reclama que não funciona?"
→ Peça para instalar Java 21 primeiro
→ Link: https://adoptium.net/

### "Quer instalador que não precisa Java?"
→ Use Windows ou VM para rodar `build-installer.bat`
→ Ou configure GitHub Actions

### "ZIP é muito grande (98 MB)?"
→ Normal, inclui todas dependências (Spring, JavaFX, etc)
→ Instalador .exe seria ~100-120 MB (inclui JRE)

### "Posso criar .exe no Linux?"
→ Não nativamente (jpackage precisa do SO alvo)
→ Use VM Windows ou GitHub Actions

---

## 📝 Instruções para o Cliente

Copie e cole no email/mensagem:

```
Olá!

Segue a aplicação Diagonal Cordéis.

INSTALAÇÃO:
1. Descompacte o arquivo DiagonalCordeis-1.0.0.zip
2. Instale o Java 21: https://adoptium.net/temurin/releases/?version=21
3. Duplo clique em DiagonalCordeis.bat

PRIMEIRO ACESSO:
Usuário: admin
Senha: admin123

OBSERVAÇÕES:
- Dados salvos automaticamente em: %APPDATA%\DiagonalCordeis\data\
- Para fazer backup: copie a pasta acima
- Para desinstalar: delete a pasta da aplicação

Dúvidas? Entre em contato!
```

---

## ✅ Checklist

Antes de enviar:

- [ ] Compilou: `./build-installer.sh`
- [ ] ZIP criado: `target/dist/DiagonalCordeis-1.0.0.zip`
- [ ] Testou localmente (Java + JAR funcionam)
- [ ] Documentou credenciais padrão
- [ ] Enviou instruções ao cliente

---

## 🎉 Resumo

**Você (Linux):**
```bash
./build-installer.sh
# Enviar: target/dist/DiagonalCordeis-1.0.0.zip
```

**Cliente (Windows):**
```
1. Instalar Java 21
2. Descompactar ZIP
3. Executar DiagonalCordeis.bat
```

**Aplicação:**
- ✅ Roda normalmente
- ✅ Banco em %APPDATA%
- ✅ Dados persistem
- ✅ Multi-usuário

---

**Tudo funcionando! 🚀**
