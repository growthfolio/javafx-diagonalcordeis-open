package com.diagonal.cordeis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Slf4j
@Component
public class DatabaseConfig implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Configurar SQLite para WAL mode e otimizações
            statement.execute("PRAGMA journal_mode = WAL");
            statement.execute("PRAGMA synchronous = NORMAL");
            statement.execute("PRAGMA cache_size = 10000");
            statement.execute("PRAGMA temp_store = memory");
            statement.execute("PRAGMA busy_timeout = 30000");
            
            log.info("SQLite configurado com WAL mode e otimizações aplicadas");
            
        } catch (Exception e) {
            log.error("Erro ao configurar SQLite", e);
        }
    }
}