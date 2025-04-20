package com.tasktracker.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource(Environment env) {
        String databaseUrl = env.getProperty("DATABASE_URL");

        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            // Convert Render-style URL to JDBC URL
            try {
                URI dbUri = new URI(databaseUrl);

                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() +
                        (dbUri.getPort() == -1 ? "" : ":" + dbUri.getPort()) +
                        dbUri.getPath();

                return DataSourceBuilder.create()
                        .url(jdbcUrl)
                        .username(username)
                        .password(password)
                        .driverClassName("org.postgresql.Driver")
                        .build();
            } catch (URISyntaxException e) {
                throw new RuntimeException("Invalid DATABASE_URL", e);
            }
        } else {
            // Use application properties
            return DataSourceBuilder.create()
                    .url(env.getProperty("spring.datasource.url"))
                    .username(env.getProperty("spring.datasource.username"))
                    .password(env.getProperty("spring.datasource.password"))
                    .driverClassName(env.getProperty("spring.datasource.driver-class-name", "org.postgresql.Driver"))
                    .build();
        }
    }
}