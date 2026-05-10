package com.contactbook.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConfig {
    private static HikariDataSource dataSource;

    static {
        try {
            Properties props = new Properties();
            try (InputStream is = DatabaseConfig.class
                    .getClassLoader()
                    .getResourceAsStream("db.properties")) {
                if (is == null) throw new RuntimeException("db.properties not found");
                props.load(is);
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));

            // Pool tuning
            config.setMaximumPoolSize(
                    Integer.parseInt(props.getProperty("pool.maximumPoolSize", "10")));
            config.setMinimumIdle(
                    Integer.parseInt(props.getProperty("pool.minimumIdle", "2")));
            config.setConnectionTimeout(
                    Long.parseLong(props.getProperty("pool.connectionTimeout", "30000")));
            config.setIdleTimeout(
                    Long.parseLong(props.getProperty("pool.idleTimeout", "600000")));
            config.setMaxLifetime(
                    Long.parseLong(props.getProperty("pool.maxLifetime", "1800000")));

            // MySQL-specific optimizations
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(config);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public static void initializeSchema() {

        String createTable = """
        CREATE TABLE IF NOT EXISTS contacts (
            id INT PRIMARY KEY AUTO_INCREMENT,
            first_name VARCHAR(100) NOT NULL,
            last_name VARCHAR(100) NOT NULL,
            email VARCHAR(255) UNIQUE,
            phone VARCHAR(30),
            address VARCHAR(255),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ) ENGINE=InnoDB
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create table
            stmt.execute(createTable);

            // Create indexes safely
            try {
                stmt.execute("""
                CREATE INDEX idx_name
                ON contacts(last_name, first_name)
            """);
            } catch (SQLException ignored) {}

            try {
                stmt.execute("""
                CREATE INDEX idx_phone
                ON contacts(phone)
            """);
            } catch (SQLException e) {
                System.out.println("Index already exists. ");
            }

            System.out.println("MySQL schema initialized.");

        } catch (SQLException e) {
            throw new RuntimeException("Schema initialization failed", e);
        }
    }
}
