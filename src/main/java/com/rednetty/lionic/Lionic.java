package com.rednetty.lionic;

import com.rednetty.lionic.population.PersonManager;
import com.rednetty.lionic.sql.service.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for Lionic.
 * Initializes database connection and manages application components.
 */
public class Lionic {
    private static final Logger LOGGER = LoggerFactory.getLogger(Lionic.class);

    private static DatabaseService databaseService;
    private static PersonManager personManager;

    /**
     * Application entry point.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        LOGGER.info("Starting Lionic application");

        try {
            // Initialize database service
            databaseService = new DatabaseService();
            LOGGER.info("Database service initialized successfully");

            // Initialize person manager
            personManager = new PersonManager(databaseService);
            LOGGER.info("Person manager initialized");

            // Start the interactive command interface
            personManager.initialize();

        } catch (Exception e) {
            LOGGER.error("Failed to initialize application: {}", e.getMessage(), e);
            System.err.println("Failed to start application: " + e.getMessage());
            System.exit(1);
        } finally {
            // Ensure clean shutdown
            shutdown();
        }
    }

    /**
     * Shuts down the application and releases resources.
     */
    private static void shutdown() {
        LOGGER.info("Shutting down Lionic application");

        if (databaseService != null) {
            try {
                databaseService.close();
                LOGGER.info("Database service shut down successfully");
            } catch (Exception e) {
                LOGGER.error("Error shutting down database service: {}", e.getMessage());
            }
        }
    }

    /**
     * @return Application's database service
     */
    public static DatabaseService getDatabaseService() {
        return databaseService;
    }

    /**
     * @return Application's person manager
     */
    public static PersonManager getPersonManager() {
        return personManager;
    }
}