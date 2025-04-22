package com.tasktracker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test") // Apply to all profiles except test
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    // Remove the explicit dataSource and entityManagerFactory beans
    // Let Spring Boot's auto-configuration handle these with the properties file
}