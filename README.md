# Diagonal Cordéis - Sistema de Gestão e Impressão

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptium.net/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)](https://openjfx.io/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen.svg)](https://spring.io/projects/spring-boot)

Sistema desktop para gestão e impressão automatizada de cordéis, desenvolvido como projeto social em alinhamento com os **Objetivos de Desenvolvimento Sustentável (ODS) 8 e 9** da ONU.

## 📖 Sobre o Projeto

Aplicação desktop desenvolvida para pequenos empreendedores da literatura de cordel, permitindo que funcionários processem e imprimem pedidos de forma autônoma, sem necessidade do conhecimento técnico completo do proprietário.

### 🎯 Motivação

O proprietário gerencia o acervo e conhece o processo de impressão detalhadamente. Com esta ferramenta, funcionários podem:
- Selecionar o SKU do pedido
- Clicar em "Imprimir"
- Sistema separa automaticamente miolo e capa
- Envia para impressoras correspondentes

Isso libera o proprietário para atividades de maior valor (estratégia, mercado, crescimento do negócio).

### 🌍 Alinhamento com ODS

- **ODS 8 - Trabalho Decente e Crescimento Econômico**: Promove autonomia operacional e eficiência em micro/pequenas empresas
- **ODS 9 - Indústria, Inovação e Infraestrutura**: Solução tecnológica que melhora processos produtivos locais

## ✨ Funcionalidades

- 📚 Cadastro de cordéis, minicordéis e coleções
- 🔍 Busca e seleção por SKU
- 🖨️ Impressão automática (miolo + capa separados)
- 🖥️ Integração com impressoras locais
- 💾 Banco de dados local SQLite (operação offline)
- 🔐 Sistema de autenticação e segurança
- 📊 Gestão de usuários e permissões
- 💼 Interface JavaFX moderna e intuitiva

## 🚀 Download e Instalação

### Para Usuários Windows

#### Opção 1: Instalador Completo (Recomendado) ⭐

**Não precisa instalar Java!**

1. Acesse a página de [Releases](https://github.com/growthfolio/javafx-diagonalcordeis-open/releases)
2. Baixe o arquivo `Diagonal Cordeis-1.0.0.exe`
3. Execute o instalador
4. Siga o assistente de instalação
5. Pronto! Aplicação instalada

#### Opção 2: Pacote JAR

**Requer Java 21 instalado**

1. Instale Java 21: https://adoptium.net/temurin/releases/?version=21
2. Baixe `DiagonalCordeis-1.0.0.zip` dos [Releases](https://github.com/growthfolio/javafx-diagonalcordeis-open/releases)
3. Descompacte o arquivo
4. Duplo clique em `DiagonalCordeis.bat`

### 📁 Localização dos Dados

O banco de dados é criado automaticamente em:
```
%APPDATA%\DiagonalCordeis\data\diagonalcordeis.db
```

Exemplo: `C:\Users\SeuNome\AppData\Roaming\DiagonalCordeis\data\`

## 🔄 Sistema de Atualização

### Verificar Atualizações

A aplicação verifica automaticamente se há novas versões disponíveis no GitHub.

### Atualizar Manualmente

1. Acesse [Releases](https://github.com/growthfolio/javafx-diagonalcordeis-open/releases)
2. Baixe a versão mais recente
3. Execute o instalador (seus dados são preservados)

### Atualização Automática (em desenvolvimento)

Sistema de auto-update será implementado em versões futuras.

## 🛠️ Tecnologias

- **Java 21** - Linguagem principal
- **JavaFX 21** - Interface gráfica
- **Spring Boot 3.2.3** - Framework backend
- **SQLite** - Banco de dados local
- **Hibernate** - ORM
- **Maven** - Gerenciamento de dependências
- **PDFBox** - Manipulação de PDFs
- **MapStruct** - Mapeamento de objetos

## 💻 Para Desenvolvedores

### Pré-requisitos

- JDK 21 ou superior
- Maven 3.6+
- Git

### Clonar e Executar

```bash
# Clonar repositório
git clone https://github.com/growthfolio/javafx-diagonalcordeis-open.git
cd javafx-diagonalcordeis-open

# Executar em modo desenvolvimento
./mvnw clean spring-boot:run

# Ou via JavaFX plugin
./mvnw javafx:run
```

### Compilar

```bash
# Compilar JAR
./mvnw clean package -DskipTests

# Gerar instalador Windows (requer Windows)
build-installer.bat

# Gerar pacote distribuível (Linux/Mac)
./build-installer.sh
```

### Build via GitHub Actions

O projeto usa GitHub Actions para compilar automaticamente:
- **Windows**: Instalador `.exe` com JRE incluído
- **Linux**: Pacote ZIP com JAR

Toda tag `v*.*.*` gera release automaticamente.

## 📝 Documentação

- [Guia de Instalação](LEIA-ME-INSTALACAO.txt) - Para usuários finais
- [Como Distribuir](COMO-DISTRIBUIR.md) - Para desenvolvedores
- [Quick Start](QUICKSTART.md) - Início rápido
- [Build no Linux](docs/BUILD_NO_LINUX.md) - Guia técnico
- [Instalador Windows](docs/GUIA_INSTALADOR_WINDOWS.md) - Detalhes técnicos

## 🔐 Primeiro Acesso

**Credenciais padrão:**
- Usuário: `admin`
- Senha: `admin123`

⚠️ **Importante**: Altere a senha no primeiro acesso!

## 🤝 Contribuindo

Este é um projeto open source social. Contribuições são bem-vindas!

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está licenciado sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 👥 Autor

**Felipe Macedo**
- GitHub: [@felipemacedo1](https://github.com/felipemacedo1)

## 🌟 Apoio ao Projeto

Este projeto faz parte de uma iniciativa social para empoderamento de pequenos empreendedores da cultura popular brasileira, especificamente literatura de cordel.

Se este projeto ajudou você ou sua empresa, considere:
- ⭐ Dar uma estrela no repositório
- 🐛 Reportar bugs e sugerir melhorias
- 📢 Compartilhar com outros que possam se beneficiar
- 🤝 Contribuir com código ou documentação

## 📞 Suporte

- **Issues**: [GitHub Issues](https://github.com/growthfolio/javafx-diagonalcordeis-open/issues)
- **Discussões**: [GitHub Discussions](https://github.com/growthfolio/javafx-diagonalcordeis-open/discussions)

## 🗺️ Roadmap

- [x] Sistema básico de gestão
- [x] Impressão automatizada
- [x] Sistema de segurança
- [x] Build automático via GitHub Actions
- [ ] Sistema de auto-update
- [ ] Relatórios avançados
- [ ] Backup automático
- [ ] Multi-idioma
- [ ] Modo escuro

---

**Desenvolvido com ❤️ para a cultura popular brasileira**

[![ODS 8](https://img.shields.io/badge/ODS-8-red.svg)](https://sdgs.un.org/goals/goal8)
[![ODS 9](https://img.shields.io/badge/ODS-9-orange.svg)](https://sdgs.un.org/goals/goal9)
