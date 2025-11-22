# Usuário Padrão do Sistema

## 📋 Informações do Usuário Administrador Padrão

Quando o sistema é executado pela primeira vez e não há usuários cadastrados no banco de dados, um usuário administrador padrão é automaticamente criado.

### 🔐 Credenciais Padrão:
- **Usuário:** `admin`
- **Senha:** `admin123`
- **Role:** `ROLE_ADMIN`
- **Profile:** `ADMIN`
- **Permissões:** Todas as permissões do sistema

### 🚀 Como usar:

1. **Primeira execução:**
   - Execute a aplicação
   - O usuário administrador será criado automaticamente
   - Use as credenciais acima para fazer login

2. **Após o primeiro login:**
   - **IMPORTANTE:** Altere a senha padrão imediatamente por questões de segurança
   - Crie outros usuários conforme necessário
   - Configure as permissões adequadas para cada usuário

### 🔒 Segurança:

⚠️ **ATENÇÃO:** 
- A senha padrão `admin123` é apenas para o primeiro acesso
- **SEMPRE altere a senha padrão** após o primeiro login
- Esta é uma prática de segurança essencial

### 📝 Logs:

O sistema registra nos logs quando o usuário padrão é criado:
```
Creating default admin user...
Default admin user created successfully!
Username: admin
Password: admin123
IMPORTANT: Please change the default password after first login!
```

### 🛠️ Comportamento:

- O usuário padrão só é criado se o banco estiver vazio (count = 0)
- Se já existirem usuários, a criação é ignorada
- O usuário tem todas as permissões do sistema
- Pode gerenciar livros, coleções, importar arquivos e criar outros usuários

### 💡 Próximos Passos:

Após o primeiro login com o usuário padrão:

1. Altere a senha na tela de configurações do usuário
2. Crie usuários específicos para diferentes funções
3. Configure permissões adequadas para cada usuário
4. Teste o sistema com diferentes níveis de acesso
