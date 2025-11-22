package com.diagonal.cordeis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Configuration
public class DataSourceConfig {

    /**
     * Determina o caminho do banco de dados baseado no ambiente de execução
     * - Desenvolvimento: ./diagonalcordeis.db (raiz do projeto)
     * - Produção Windows: %APPDATA%/DiagonalCordeis/data/diagonalcordeis.db
     * - Produção Linux: ~/.diagonalcordeis/data/diagonalcordeis.db
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        String dbPath = getDatabasePath();
        String jdbcUrl = String.format("jdbc:sqlite:%s?journal_mode=WAL&busy_timeout=30000&synchronous=NORMAL", dbPath);
        
        log.info("========================================");
        log.info("Configurando banco de dados SQLite");
        log.info("Caminho: {}", dbPath);
        log.info("========================================");
        
        return DataSourceBuilder.create()
                .driverClassName("org.sqlite.JDBC")
                .url(jdbcUrl)
                .build();
    }

    private String getDatabasePath() {
        // Verifica se está em ambiente de desenvolvimento (arquivo existe na raiz)
        File devDb = new File("diagonalcordeis.db");
        if (devDb.exists() && !isRunningFromJar()) {
            log.info("Ambiente de desenvolvimento detectado");
            return "diagonalcordeis.db";
        }

        // Ambiente de produção - usar pasta de dados do aplicativo
        String dataDir = getApplicationDataDirectory();
        Path dbPath = Paths.get(dataDir, "diagonalcordeis.db");
        
        // Criar diretório se não existir
        try {
            Files.createDirectories(Paths.get(dataDir));
            log.info("Diretório de dados criado/verificado: {}", dataDir);
        } catch (IOException e) {
            log.error("Erro ao criar diretório de dados: {}", dataDir, e);
            throw new RuntimeException("Não foi possível criar diretório de dados", e);
        }

        return dbPath.toString();
    }

    private String getApplicationDataDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            // Windows: C:\Users\<user>\AppData\Roaming\DiagonalCordeis\data
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                return Paths.get(appData, "DiagonalCordeis", "data").toString();
            }
        } else if (os.contains("mac")) {
            // macOS: ~/Library/Application Support/DiagonalCordeis/data
            String home = System.getProperty("user.home");
            return Paths.get(home, "Library", "Application Support", "DiagonalCordeis", "data").toString();
        }
        
        // Linux e outros: ~/.diagonalcordeis/data
        String home = System.getProperty("user.home");
        return Paths.get(home, ".diagonalcordeis", "data").toString();
    }

    private boolean isRunningFromJar() {
        String className = this.getClass().getName().replace('.', '/');
        String classJar = this.getClass().getResource("/" + className + ".class").toString();
        return classJar.startsWith("jar:");
    }
}
