package com.diagonal.cordeis.security;

import com.diagonal.cordeis.model.Role;
import com.diagonal.cordeis.model.User;
import com.diagonal.cordeis.model.UserProfile;
import com.diagonal.cordeis.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Verificar se já existe algum usuário no banco
        if (userRepository.count() == 0) {
            createDefaultAdminUser();
        } else {
            log.info("Database already has users, skipping default user creation");
        }
    }

    private void createDefaultAdminUser() {
        log.info("Creating default admin user...");
        
        try {
            // Criar usuário administrador padrão
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setNome("Administrador");
            adminUser.setActive(true);
            adminUser.setProfile(UserProfile.ADMIN);
            
            // Adicionar Role de ADMIN
            Set<Role> roles = new HashSet<>();
            roles.add(Role.ROLE_ADMIN);
            adminUser.setRoles(roles);
            
            // Salvar no banco
            userRepository.save(adminUser);
            
            log.info("Default admin user created successfully!");
            log.info("Username: admin");
            log.info("Password: admin123");
            log.info("IMPORTANT: Please change the default password after first login!");
            
        } catch (Exception e) {
            log.error("Error creating default admin user", e);
        }
    }
}
