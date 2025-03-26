package com.rednetty.lionic.sql.config;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.rednetty.lionic.sql.exception.DatabaseException;
import com.rednetty.lionic.sql.exception.DatabaseException.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Loads database configuration from various sources (JSON file, environment variables).
 * Supports fallback mechanisms and validation of configuration parameters.
 */
public class ConfigLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);

    // Default configuration file locations to check
    private static final String[] CONFIG_LOCATIONS = {
            "./config/database.json",
            "./src/main/resources/database.json"
    };

    private final Gson gson;

    /**
     * Creates a new ConfigLoader with default Gson instance.
     */
    public ConfigLoader() {
        this.gson = new Gson();
    }

    /**
     * Loads database configuration from the first available source.
     * Checks multiple file locations and falls back to environment variables if needed.
     *
     * @return A configured DatabaseConfig object
     * @throws DatabaseException if configuration cannot be loaded from any source
     */
    public DatabaseConfig loadConfig() {
        // Try to load from config file first
        for (String location : CONFIG_LOCATIONS) {
            DatabaseConfig config = loadFromFile(location);
            if (config != null) {
                LOGGER.info("Loaded database configuration from {}", location);
                return config;
            }
        }

        // Fall back to environment variables
        DatabaseConfig config = loadFromEnvironment();
        if (config != null) {
            LOGGER.info("Loaded database configuration from environment variables");
            return config;
        }

        // No configuration found
        throw new DatabaseException(
                "Failed to load database configuration from any source",
                ErrorType.CONFIGURATION_ERROR
        );
    }

    /**
     * Loads configuration from a JSON file.
     *
     * @param filePath Path to the JSON configuration file
     * @return DatabaseConfig object or null if file cannot be loaded
     */
    private DatabaseConfig loadFromFile(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            LOGGER.debug("Configuration file not found: {}", filePath);
            return null;
        }

        try (FileReader reader = new FileReader(filePath)) {
            ConfigFileFormat format = gson.fromJson(reader, ConfigFileFormat.class);
            return convertToDatabaseConfig(format);
        } catch (IOException e) {
            LOGGER.warn("Error reading configuration file: {}", e.getMessage());
            return null;
        } catch (JsonSyntaxException e) {
            LOGGER.warn("Invalid JSON format in configuration file: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Loads configuration from environment variables.
     *
     * @return DatabaseConfig object or null if required variables are not set
     */
    private DatabaseConfig loadFromEnvironment() {
        String dbType = System.getenv("DB_TYPE");
        String hostName = System.getenv("DB_HOST");
        String dbName = System.getenv("DB_NAME");
        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");

        // Check if required variables are set
        if (dbType == null || hostName == null || dbName == null ||
                username == null || password == null) {
            LOGGER.debug("Not all required environment variables are set");
            return null;
        }

        // Parse optional variables with defaults
        int port = parseIntEnv("DB_PORT", 5432);
        int poolSize = parseIntEnv("DB_POOL_SIZE", 10);
        long connTimeout = parseLongEnv("DB_CONN_TIMEOUT", 30000);
        long idleTimeout = parseLongEnv("DB_IDLE_TIMEOUT", 600000);

        return new DatabaseConfig.Builder()
                .dbType(dbType)
                .hostName(hostName)
                .dbName(dbName)
                .username(username)
                .password(password)
                .port(port)
                .connectionPoolSize(poolSize)
                .connectionTimeout(connTimeout)
                .idleTimeout(idleTimeout)
                .build();
    }

    /**
     * Helper method to parse integer environment variables with defaults.
     */
    private int parseIntEnv(String name, int defaultValue) {
        String value = System.getenv(name);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid integer format for {}: {}", name, value);
            return defaultValue;
        }
    }

    /**
     * Helper method to parse long environment variables with defaults.
     */
    private long parseLongEnv(String name, long defaultValue) {
        String value = System.getenv(name);
        if (value == null) return defaultValue;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid long format for {}: {}", name, value);
            return defaultValue;
        }
    }

    /**
     * Converts config file format to DatabaseConfig object.
     */
    private DatabaseConfig convertToDatabaseConfig(ConfigFileFormat format) {
        return new DatabaseConfig.Builder()
                .dbType(format.dbType)
                .hostName(format.hostName)
                .dbName(format.dbName)
                .username(format.username)
                .password(format.password)
                .port(format.port)
                .connectionPoolSize(format.connectionPoolSize)
                .connectionTimeout(format.connectionTimeout)
                .idleTimeout(format.idleTimeout)
                .build();
    }

    /**
     * Internal class that matches the JSON structure of the config file.
     */
    private static class ConfigFileFormat {
        String dbType;
        String hostName;
        String dbName;
        String username;
        String password;
        int port = 5432;
        int connectionPoolSize = 10;
        long connectionTimeout = 30000;
        long idleTimeout = 600000;
    }
}