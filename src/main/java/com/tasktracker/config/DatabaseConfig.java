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
        logger.info("DATABASE_URL from environment: {}",
                databaseUrl != null ? databaseUrl.replaceAll(":[^:@]+@", ":******@") : "null");
        logger.info("JDBC_DATABASE_URL from environment: {}",
                jdbcDatabaseUrl != null ? jdbcDatabaseUrl.replaceAll(":[^:@]+@", ":******@") : "null");

        HikariDataSource dataSource = new HikariDataSource();

        // Use JDBC_DATABASE_URL from Render if available
        if (jdbcDatabaseUrl != null) {
            logger.info("Using JDBC_DATABASE_URL");
            dataSource.setJdbcUrl(jdbcDatabaseUrl);
            dataSource.setUsername(jdbcUsername != null ? jdbcUsername : username);
            dataSource.setPassword(jdbcPassword != null ? jdbcPassword : password);
            dataSource.setDriverClassName("org.postgresql.Driver");
        }
        // Fall back to DATABASE_URL parsing
        else if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
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
                dataSource.setDriverClassName("org.postgresql.Driver");
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

            // Set the appropriate driver class name based on the JDBC URL
            if (jdbcUrl.startsWith("jdbc:h2:")) {
                dataSource.setDriverClassName("org.h2.Driver");
            } else if (jdbcUrl.startsWith("jdbc:postgresql:")) {
                dataSource.setDriverClassName("org.postgresql.Driver");
            } else {
                logger.warn("Unknown database type in URL: {}, defaulting to PostgreSQL driver", jdbcUrl);
                dataSource.setDriverClassName("org.postgresql.Driver");
            }
        }

        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.tasktracker.model");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", determineDialect());

        // Create tables in development, validate in production
        boolean isDev = Arrays.asList(environment.getActiveProfiles()).contains("dev");
        if (isDev) {
            properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        } else {
            properties.setProperty("hibernate.hbm2ddl.auto", "validate");
        }

        em.setJpaProperties(properties);

        return em;
    }

    private String determineDialect() {
        if (jdbcUrl != null && jdbcUrl.startsWith("jdbc:h2:")) {
            return "org.hibernate.dialect.H2Dialect";
        } else if (jdbcDatabaseUrl != null ||
                (databaseUrl != null && databaseUrl.startsWith("postgresql://"))) {
            // Don't set dialect for PostgreSQL - let Hibernate detect it
            return null;
        } else {
            // Default case
            return null;
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
    @ConditionalOnProperty(name = "spring.profiles.active", havingValue = "prod", matchIfMissing = true)
    public Flyway flywayProd() {
        return Flyway.configure()
                .dataSource(dataSource())
                .locations("classpath:db/migration/postgresql")
                .baselineOnMigrate(true)
                .load();
    }
}