package com.tasktracker.config;

import com.zaxxer.hikari.HikariDataSource;
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

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    @Primary
    public HikariDataSource dataSource() {
        logger.info("DATABASE_URL from environment: {}",
                databaseUrl != null ? databaseUrl.replaceAll(":[^:@]+@", ":******@") : "null");

        HikariDataSource dataSource = new HikariDataSource();

        // Check if we need to convert the Render-style URL
        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            try {
                URI dbUri = new URI(databaseUrl);

                String dbUsername = dbUri.getUserInfo().split(":")[0];
                String dbPassword = dbUri.getUserInfo().split(":")[1];
                String convertedUrl = "jdbc:postgresql://" + dbUri.getHost() +
                        (dbUri.getPort() == -1 ? "" : ":" + dbUri.getPort()) +
                        dbUri.getPath();

                logger.info("Converted JDBC URL: {}", convertedUrl.replaceAll("password=.*?(&|$)", "password=****$1"));

                dataSource.setJdbcUrl(convertedUrl);
                dataSource.setUsername(dbUsername);
                dataSource.setPassword(dbPassword);
            } catch (URISyntaxException e) {
                logger.error("Failed to parse DATABASE_URL", e);
                throw new RuntimeException("Invalid DATABASE_URL", e);
            }
        } else {
            // Fall back to application properties
            logger.info("Using application properties JDBC URL: {}", jdbcUrl);
            dataSource.setJdbcUrl(jdbcUrl);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
        }

        dataSource.setDriverClassName("org.postgresql.Driver");
        return dataSource;
    }
}