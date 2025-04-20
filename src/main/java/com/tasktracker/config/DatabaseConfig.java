package com.tasktracker.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Bean
    @Primary
    public DataSource dataSource() {
        logger.info("DATABASE_URL from environment: {}",
                databaseUrl != null ? databaseUrl.replaceAll(":[^:@]+@", ":******@") : "null");

        HikariDataSource dataSource = new HikariDataSource();

        // Check if we need to convert the Render-style URL
        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            try {
                URI dbUri = new URI(databaseUrl);

                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() +
                        (dbUri.getPort() == -1 ? "" : ":" + dbUri.getPort()) +
                        dbUri.getPath();

                logger.info("Converted JDBC URL: {}", jdbcUrl.replaceAll("password=.*?(&|$)", "password=****$1"));

                dataSource.setJdbcUrl(jdbcUrl);
                dataSource.setUsername(username);
                dataSource.setPassword(password);
            } catch (URISyntaxException e) {
                logger.error("Failed to parse DATABASE_URL", e);
                throw new RuntimeException("Invalid DATABASE_URL", e);
            }
        } else {
            // Fall back to application properties if no DATABASE_URL
            logger.info("Using application properties for database connection");
            // These will be injected from properties by Spring Boot
            dataSource.setJdbcUrl("${spring.datasource.url}");
            dataSource.setUsername("${spring.datasource.username}");
            dataSource.setPassword("${spring.datasource.password}");
        }

        dataSource.setDriverClassName("org.postgresql.Driver");
        return dataSource;
    }
}