package com.rednetty.lionic.sql.config;

import java.util.Objects;

/**
 * Represents database connection configuration parameters.
 * Immutable class that stores all necessary information to establish
 * a database connection.
 */
public class DatabaseConfig {
    private final String dbType;
    private final String hostName;
    private final String dbName;
    private final String username;
    private final String password;
    private final int port;
    private final int connectionPoolSize;
    private final long connectionTimeout;
    private final long idleTimeout;

    /**
     * Creates a new DatabaseConfig with the specified parameters.
     *
     * @param builder The builder containing configuration values
     */
    private DatabaseConfig(Builder builder) {
        this.dbType = builder.dbType;
        this.hostName = builder.hostName;
        this.dbName = builder.dbName;
        this.username = builder.username;
        this.password = builder.password;
        this.port = builder.port;
        this.connectionPoolSize = builder.connectionPoolSize;
        this.connectionTimeout = builder.connectionTimeout;
        this.idleTimeout = builder.idleTimeout;
    }

    /**
     * @return Database type (e.g., "postgresql", "mysql")
     */
    public String getDbType() {
        return dbType;
    }

    /**
     * @return Database server hostname or IP address
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * @return Name of the database to connect to
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * @return Username for database authentication
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return Password for database authentication
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return Port number for database connection
     */
    public int getPort() {
        return port;
    }

    /**
     * @return Maximum number of connections in the pool
     */
    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    /**
     * @return Connection timeout in milliseconds
     */
    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * @return Maximum idle time for connections in milliseconds
     */
    public long getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Constructs the JDBC URL based on the configuration.
     *
     * @return Fully formatted JDBC URL
     */
    public String getJdbcUrl() {
        return String.format("jdbc:%s://%s:%d/%s", dbType, hostName, port, dbName);
    }

    /**
     * Builder for DatabaseConfig to support a fluent API for creating instances.
     */
    public static class Builder {
        private String dbType;
        private String hostName;
        private String dbName;
        private String username;
        private String password;
        private int port = 5432; // Default PostgreSQL port
        private int connectionPoolSize = 10; // Default pool size
        private long connectionTimeout = 30000; // 30 seconds
        private long idleTimeout = 600000; // 10 minutes

        public Builder dbType(String dbType) {
            this.dbType = dbType;
            return this;
        }

        public Builder hostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public Builder dbName(String dbName) {
            this.dbName = dbName;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder connectionPoolSize(int connectionPoolSize) {
            this.connectionPoolSize = connectionPoolSize;
            return this;
        }

        public Builder connectionTimeout(long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder idleTimeout(long idleTimeout) {
            this.idleTimeout = idleTimeout;
            return this;
        }

        /**
         * Validates and builds a new DatabaseConfig instance.
         *
         * @return A new DatabaseConfig instance
         * @throws IllegalStateException if any required field is missing
         */
        public DatabaseConfig build() {
            // Validate required fields
            Objects.requireNonNull(dbType, "Database type cannot be null");
            Objects.requireNonNull(hostName, "Host name cannot be null");
            Objects.requireNonNull(dbName, "Database name cannot be null");
            Objects.requireNonNull(username, "Username cannot be null");
            Objects.requireNonNull(password, "Password cannot be null");

            if (port <= 0) {
                throw new IllegalStateException("Port must be a positive number");
            }

            return new DatabaseConfig(this);
        }
    }
}