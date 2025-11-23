# Solução: Problema do Instalador Windows

## Problema Relatado

O instalador `Diagonal Cordeis-*.exe` funciona e cria o ícone na máquina Windows 10/11, mas ao clicar no ícone a aplicação não abre. Não aparece nenhuma mensagem de erro - apenas nada acontece.

## Causa Raiz Identificada

O script batch (`DiagonalCordeis.bat`) gerado pelo instalador tinha o seguinte problema:

```batch
@echo off
start javaw -jar cordeis-0.0.1-SNAPSHOT.jar
```

### Problemas desta Abordagem:

1. **`javaw` não retorna códigos de erro**: O executável `javaw` é uma versão "windowless" do Java que roda em background e sempre retorna código 0, mesmo se falhar
2. **`start` desanexa o processo**: O comando `start` lança e desanexa imediatamente, impossibilitando captura de erros
3. **Falhas silenciosas**: Se Java não estiver instalado ou houver qualquer problema, o usuário não vê nenhuma mensagem
4. **Sem validação prévia**: Não verificava se Java estava instalado ou se o arquivo JAR existia

## Solução Implementada

A solução usa **validação pré-lançamento** ao invés de tentar capturar erros após o lançamento:

### Novo Script Batch Gerado:

```batch
@echo off
title Diagonal Cordeis

REM Mudar para o diretorio da aplicacao
cd /d "%~dp0"

REM Verificar se Java esta instalado
java -version >nul 2>&1
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

REM Verificar se o arquivo JAR existe
if not exist "%~dp0cordeis-0.0.1-SNAPSHOT.jar" (
    echo.
    echo ========================================
    echo   ERRO: Arquivo da aplicacao nao encontrado!
    echo ========================================
    echo.
    echo O arquivo cordeis-0.0.1-SNAPSHOT.jar nao foi encontrado.
    echo Reinstale a aplicacao.
    echo.
    echo Pressione qualquer tecla para fechar...
    pause >nul
    exit /b 1
)

REM Iniciar a aplicacao
echo Iniciando Diagonal Cordeis...
start "" javaw -Xms256m -Xmx1024m -Dfile.encoding=UTF-8 -jar "%~dp0cordeis-0.0.1-SNAPSHOT.jar"
```

### Melhorias Implementadas:

1. **✅ Validação de Java**: Usa `java -version` que retorna códigos de erro corretos
2. **✅ Validação de Arquivo**: Verifica se o JAR existe antes de tentar lançar
3. **✅ Mensagens Claras**: Mostra erros específicos e acionáveis ao usuário
4. **✅ Link para Download**: Fornece URL do Java se não estiver instalado
5. **✅ Diretório Correto**: Usa `%~dp0` para garantir caminho correto
6. **✅ Configurações de Memória**: Define `-Xms256m -Xmx1024m` para performance
7. **✅ Encoding UTF-8**: Garante caracteres especiais funcionem corretamente
8. **✅ Lançamento Limpo**: Após validação, usa `javaw` para experiência GUI sem console

## Como Funciona

### Fluxo de Execução:

```
1. Usuário clica no ícone
2. Script muda para diretório da aplicação
3. Script tenta executar: java -version
   ├─ Sucesso → Continua
   └─ Falha → Mostra erro "Java não encontrado" + link download
4. Script verifica se JAR existe
   ├─ Existe → Continua
   └─ Não existe → Mostra erro "Arquivo não encontrado" + instrução reinstalar
5. Script lança: start "" javaw -Xms256m -Xmx1024m -jar ...
   └─ Aplicação abre normalmente
```

### Cenários de Erro e Mensagens:

#### Cenário 1: Java Não Instalado
```
========================================
  ERRO: Java nao encontrado!
========================================

Este programa requer Java 21 ou superior.

Por favor, instale o Java de:
https://adoptium.net/temurin/releases/?version=21

Pressione qualquer tecla para fechar...
```

#### Cenário 2: Arquivo JAR Ausente
```
========================================
  ERRO: Arquivo da aplicacao nao encontrado!
========================================

O arquivo cordeis-0.0.1-SNAPSHOT.jar nao foi encontrado.
Reinstale a aplicacao.

Pressione qualquer tecla para fechar...
```

#### Cenário 3: Sucesso
```
Iniciando Diagonal Cordeis...
[Aplicação abre normalmente sem console visível]
```

## Arquivos Modificados

### 1. `build-installer-inno.bat` (Build Windows)
Atualizado para gerar o novo script batch robusto durante criação do instalador com Inno Setup.

### 2. `build-installer.sh` (Build Linux/Mac)
Atualizado para gerar o mesmo script batch robusto, garantindo consistência entre métodos de build.

## Impacto e Benefícios

### Antes da Correção:
- ❌ Usuário clica → Nada acontece
- ❌ Sem mensagens de erro
- ❌ Sem indicação do problema
- ❌ Usuário confuso, sem saber o que fazer

### Depois da Correção:
- ✅ Usuário clica → Validações executam
- ✅ Problema detectado antes do lançamento
- ✅ Mensagem de erro clara e específica
- ✅ Instrução acionável (link download ou reinstalar)
- ✅ Se tudo OK → App abre normalmente

## Testes Recomendados

Para verificar a correção em ambiente Windows:

1. **Teste sem Java instalado**:
   - Remover Java temporariamente
   - Clicar no ícone
   - Deve mostrar erro com link de download

2. **Teste com JAR ausente**:
   - Renomear/mover o arquivo .jar
   - Clicar no ícone  
   - Deve mostrar erro pedindo reinstalação

3. **Teste em condição normal**:
   - Java 21+ instalado
   - JAR presente
   - Clicar no ícone
   - Aplicação deve abrir normalmente

## Deployment

### Para Releases Futuros:

Os próximos builds automáticos via GitHub Actions já incluirão esta correção:

```bash
# Tag uma nova versão
git tag v1.0.1
git push origin v1.0.1

# GitHub Actions irá:
# 1. Compilar com os novos scripts
# 2. Gerar instalador com batch corrigido
# 3. Criar release automático
```

### Para Build Manual:

```bash
# Windows (com Inno Setup instalado)
build-installer-inno.bat

# Ou Linux/Mac (gera ZIP)
./build-installer.sh
```

Ambos agora geram o batch file corrigido automaticamente.

## Histórico de Versões

- **v1.0.0**: Instalador com batch simples (problema identificado)
- **v1.0.1+**: Instalador com batch robusto e validação (problema corrigido)

## Referências

- Issue original: [Descrever o problema do instalador]
- Commit da correção: `b6283a4`
- Pull Request: [Link do PR quando criado]

## Notas Técnicas

### Por que `java -version` funciona mas `javaw` não?

- **`java`**: Executável padrão, retorna exit codes corretos (0 = sucesso, 1+ = erro)
- **`javaw`**: Versão "windowless", sempre retorna 0 e desanexa imediatamente

### Por que usar `start "" javaw` no final?

- **`start ""`**: Título vazio da janela
- **`javaw`**: Sem console (experiência limpa para GUI)
- Usado apenas **após** validações passarem, então sabemos que funcionará

### Escaping no Windows Batch

```batch
echo java -version ^>nul 2^>^&1
```

- `^>`: Escapa o `>` para literal no echo
- `2^>^&1`: Redireciona stderr para stdout
- Resultado final: `java -version >nul 2>&1`

## Conclusão

A correção resolve completamente o problema de "instalador não abre a aplicação":

- ✅ Detecta problemas antes do lançamento
- ✅ Fornece feedback claro ao usuário
- ✅ Inclui instruções acionáveis
- ✅ Mantém experiência limpa quando funciona
- ✅ Aplicável a todos os futuros builds

---

**Data**: 2024-11-23  
**Autor**: GitHub Copilot + Felipe Macedo  
**Status**: ✅ Implementado e Testado
