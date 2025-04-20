package com.tasktracker.config;

import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Properties;

@Configuration
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${JDBC_DATABASE_URL:#{null}}")
    private String jdbcDatabaseUrl;

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${JDBC_DATABASE_USERNAME:#{null}}")
    private String jdbcUsername;

    @Value("${JDBC_DATABASE_PASSWORD:#{null}}")
    private String jdbcPassword;

    private final Environment environment;

    public DatabaseConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    @Primary
    public HikariDataSource dataSource() {
        try {
            logger.info("DATABASE_URL from environment: {}",
                    databaseUrl != null ? databaseUrl.replaceAll(":[^:@]+@", ":******@") : "null");
            logger.info("JDBC_DATABASE_URL from environment: {}",
                    jdbcDatabaseUrl != null ? jdbcDatabaseUrl.replaceAll(":[^:@]+@", ":******@") : "null");
            logger.info("Spring datasource URL: {}", jdbcUrl);

            HikariDataSource dataSource = new HikariDataSource();

            // Docker environment uses SPRING_DATASOURCE_URL
            if (jdbcUrl != null && !jdbcUrl.isEmpty()) {
                logger.info("Using SPRING_DATASOURCE_URL from properties: {}", jdbcUrl);
                dataSource.setJdbcUrl(jdbcUrl);
                dataSource.setUsername(username);
                dataSource.setPassword(password);

                if (jdbcUrl.startsWith("jdbc:h2:")) {
                    dataSource.setDriverClassName("org.h2.Driver");
                } else if (jdbcUrl.startsWith("jdbc:postgresql:")) {
                    dataSource.setDriverClassName("org.postgresql.Driver");
                }
            }
            // Use JDBC_DATABASE_URL from Render if available
            else if (jdbcDatabaseUrl != null && !jdbcDatabaseUrl.isEmpty()) {
                logger.info("Using JDBC_DATABASE_URL for database connection");
                dataSource.setJdbcUrl(jdbcDatabaseUrl);
                dataSource.setUsername(jdbcUsername != null ? jdbcUsername : username);
                dataSource.setPassword(jdbcPassword != null ? jdbcPassword : password);
                dataSource.setDriverClassName("org.postgresql.Driver");
            }
            // Convert DATABASE_URL to JDBC format if needed
            else if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
                try {
                    logger.info("Parsing DATABASE_URL for database connection");
                    URI dbUri = new URI(databaseUrl);

                    String dbUsername = dbUri.getUserInfo().split(":")[0];
                    String dbPassword = dbUri.getUserInfo().split(":")[1];
                    String convertedUrl = "jdbc:postgresql://" + dbUri.getHost() +
                            (dbUri.getPort() == -1 ? "" : ":" + dbUri.getPort()) +
                            dbUri.getPath();

                    logger.info("Converted JDBC URL: {}",
                            convertedUrl.replaceAll("password=.*?(&|$)", "password=****$1"));

                    dataSource.setJdbcUrl(convertedUrl);
                    dataSource.setUsername(dbUsername);
                    dataSource.setPassword(dbPassword);
                    dataSource.setDriverClassName("org.postgresql.Driver");
                } catch (URISyntaxException e) {
                    logger.error("Failed to parse DATABASE_URL", e);
                    throw new RuntimeException("Invalid DATABASE_URL", e);
                }
            }
            // DOCKER FALLBACK - Use default PostgreSQL settings when in Docker
            else if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
                logger.info("Using default Docker PostgreSQL settings");
                dataSource.setJdbcUrl("jdbc:postgresql://db:5432/postgres");
                dataSource.setUsername("postgres");
                dataSource.setPassword("postgres");
                dataSource.setDriverClassName("org.postgresql.Driver");
            } else {
                // Default to H2 in-memory database for development
                logger.info("Using fallback H2 in-memory database");
                dataSource.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
                dataSource.setUsername("sa");
                dataSource.setPassword("");
                dataSource.setDriverClassName("org.h2.Driver");
            }

            return dataSource;
        } catch (Exception e) {
            logger.error("Failed to create dataSource bean", e);
            throw e;
        }
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        try {
            LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
            em.setDataSource(dataSource());
            em.setPackagesToScan("com.tasktracker.model");

            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            em.setJpaVendorAdapter(vendorAdapter);

            Properties properties = new Properties();

            boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");

            // Set dialect based on environment
            if (isProd) {
                properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
                properties.setProperty("hibernate.hbm2ddl.auto", "update");
            } else {
                properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
                properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
            }

            em.setJpaProperties(properties);

            return em;
        } catch (Exception e) {
            logger.error("Failed to create entityManagerFactory bean", e);
            throw e;
        }
    }

    @Bean
    @ConditionalOnProperty(name = "spring.profiles.active", havingValue = "dev")
    public Flyway flywayDev() {
        // For dev, let Hibernate create tables
        return Flyway.configure()
                .dataSource(dataSource())
                .locations("classpath:db/migration/h2")
                .baselineOnMigrate(true)
                .load();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.profiles.active", havingValue = "prod")
    public Flyway flywayProd() {
        try {
            return Flyway.configure()
                    .dataSource(dataSource())
                    .locations("classpath:db/migration/postgresql")
                    .baselineOnMigrate(true)
                    .load();
        } catch (Exception e) {
            logger.error("Failed to configure Flyway for production", e);
            // Return null instead of propagating the exception
            return null;
        }
    }
}