package com.rednetty.lionic.sql.service;

import com.rednetty.lionic.population.Person;
import com.rednetty.lionic.sql.ConnectionManager;
import com.rednetty.lionic.sql.config.ConfigLoader;
import com.rednetty.lionic.sql.config.DatabaseConfig;
import com.rednetty.lionic.sql.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service that provides high-level database operations.
 * This class serves as the main entry point for the application to interact with the database.
 */
public class DatabaseService implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseService.class);

    private final ConnectionManager connectionManager;
    private final PersonRepository personRepository;

    /**
     * Creates a new DatabaseService with default configuration.
     */
    public DatabaseService() {
        this(new ConfigLoader().loadConfig());
    }

    /**
     * Creates a new DatabaseService with the provided configuration.
     *
     * @param config Database configuration
     */
    public DatabaseService(DatabaseConfig config) {
        LOGGER.info("Initializing Database Service");
        this.connectionManager = new ConnectionManager(config);
        this.personRepository = new PersonRepository(connectionManager);
        initialize();
    }

    /**
     * Initializes the database service and all repositories.
     */
    private void initialize() {
        LOGGER.info("Initializing database repositories");
        personRepository.initialize();
    }

    /**
     * Retrieves a person by ID.
     *
     * @param id Person ID
     * @return Optional containing the person if found, or empty if not found
     */
    public Optional<Person> getPerson(long id) {
        return personRepository.findById(id);
    }

    /**
     * Retrieves all people from the database.
     *
     * @return List of all people
     */
    public List<Person> getAllPeople() {
        return personRepository.findAll();
    }

    /**
     * Saves a person to the database.
     *
     * @param person Person to save
     * @return Saved person with any updates
     */
    public Person savePerson(Person person) {
        return personRepository.save(person);
    }

    /**
     * Saves multiple people to the database in a single operation.
     *
     * @param people List of people to save
     */
    public void savePeople(List<Person> people) {
        personRepository.saveAll(people);
    }

    /**
     * Deletes a person from the database.
     *
     * @param id Person ID
     * @return true if person was deleted, false if not found
     */
    public boolean deletePerson(long id) {
        return personRepository.deleteById(id);
    }

    /**
     * Deletes all people from the database.
     *
     * @return Number of people deleted
     */
    public int deleteAllPeople() {
        return personRepository.deleteAll();
    }

    /**
     * Searches for people by name (partial match on first or last name).
     *
     * @param namePattern Name pattern to search for
     * @return List of matching people
     */
    public List<Person> findPeopleByName(String namePattern) {
        return personRepository.findByName(namePattern);
    }

    /**
     * For backward compatibility - stores all people as JSON.
     *
     * @param people List of people to store
     * @deprecated Use savePeople() instead
     */
    @Deprecated
    public void storeAllPeopleAsJson(ArrayList<Person> people) {
        personRepository.storeAllAsJson(people);
    }

    /**
     * For backward compatibility - retrieves all people from JSON storage.
     *
     * @return List of people from legacy storage
     * @deprecated Use getAllPeople() instead
     */
    @Deprecated
    public ArrayList<Person> retrieveAllPeopleFromJson() {
        return personRepository.retrieveAllFromJson();
    }

    /**
     * Closes the database service and releases resources.
     */
    @Override
    public void close() {
        LOGGER.info("Shutting down Database Service");
        connectionManager.close();
    }
}