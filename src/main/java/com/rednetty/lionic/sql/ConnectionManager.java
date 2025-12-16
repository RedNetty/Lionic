package com.rednetty.lionic.sql;

import com.rednetty.lionic.sql.config.DatabaseConfig;
import com.rednetty.lionic.sql.exception.DatabaseException;
import com.rednetty.lionic.sql.exception.DatabaseException.ErrorType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages database connections using a connection pool.
 * This class is responsible for creating, providing, and managing
 * the lifecycle of database connections in a thread-safe manner.
 */
public class ConnectionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

    private final HikariDataSource dataSource;
    private final DatabaseConfig config;

    /**
     * Creates a connection manager with the provided configuration.
     *
     * @param config Database configuration
     * @throws DatabaseException if connection pool setup fails
     */
    public ConnectionManager(DatabaseConfig config) {
        this.config = config;
        this.dataSource = createDataSource(config);
        validateConnection();
        LOGGER.info("Database connection pool initialized successfully");
    }

    /**
     * Creates and configures the HikariCP data source.
     *
     * @param config Database configuration
     * @return Configured HikariDataSource
     */
    private HikariDataSource createDataSource(DatabaseConfig config) {
        try {
            HikariConfig hikariConfig = new HikariConfig();

            // Set connection parameters
            hikariConfig.setJdbcUrl(config.getJdbcUrl());
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword());

            // Configure pool properties
            hikariConfig.setMaximumPoolSize(config.getConnectionPoolSize());
            hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
            hikariConfig.setIdleTimeout(config.getIdleTimeout());

            // Add connection testing
            hikariConfig.setConnectionTestQuery("SELECT 1");
            hikariConfig.setMinimumIdle(2);

            // Set pool name for better monitoring
            hikariConfig.setPoolName("LionicDBPool");

            return new HikariDataSource(hikariConfig);
        } catch (Exception e) {
            throw new DatabaseException(
                    "Failed to create connection pool: " + e.getMessage(),
                    e,
                    ErrorType.CONNECTION_FAILED
            );
        }
    }

    /**
     * Validates that the connection pool can establish connections.
     *
     * @throws DatabaseException if validation fails
     */
    private void validateConnection() {
        try (Connection conn = getConnection()) {
            if (conn == null || conn.isClosed()) {
                throw new DatabaseException(
                        "Failed to validate database connection",
                        ErrorType.CONNECTION_FAILED
                );
            }
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Failed to validate database connection: " + e.getMessage(),
                    e,
                    ErrorType.CONNECTION_FAILED
            );
        }
    }

    /**
     * Gets a connection from the connection pool.
     *
     * @return Database connection
     * @throws DatabaseException if connection cannot be obtained
     */
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Failed to get database connection: " + e.getMessage(),
                    e,
                    ErrorType.CONNECTION_FAILED
            );
        }
    }

    /**
     * @return The data source for direct use when needed
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @return The database configuration
     */
    public DatabaseConfig getConfig() {
        return config;
    }

    /**
     * Closes the connection pool and releases resources.
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            LOGGER.info("Shutting down database connection pool");
            dataSource.close();
        }
    }
}