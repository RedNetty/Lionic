package com.rednetty.lionic.sql.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rednetty.lionic.population.Person;
import com.rednetty.lionic.sql.ConnectionManager;
import com.rednetty.lionic.sql.exception.DatabaseException;
import com.rednetty.lionic.sql.exception.DatabaseException.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Person entities in the database.
 * Provides CRUD operations and specialized queries for Person objects.
 */
public class PersonRepository extends Repository<Person, Long> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonRepository.class);

    // SQL statements
    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS people (" +
                    "id SERIAL PRIMARY KEY," +
                    "person_id BIGINT," +
                    "first_name VARCHAR(255)," +
                    "last_name VARCHAR(255)," +
                    "age INT," +
                    "email VARCHAR(255)," +
                    "additional_data TEXT" +
                    ")";

    private static final String FIND_BY_ID_SQL =
            "SELECT * FROM people WHERE person_id = ?";

    private static final String FIND_ALL_SQL =
            "SELECT * FROM people";

    private static final String INSERT_SQL =
            "INSERT INTO people (person_id, first_name, last_name, age, email, additional_data) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE people SET first_name = ?, last_name = ?, age = ?, " +
                    "email = ?, additional_data = ? WHERE person_id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM people WHERE person_id = ?";

    private static final String COUNT_BY_ID_SQL =
            "SELECT COUNT(*) FROM people WHERE person_id = ?";

    private static final String FIND_BY_NAME_SQL =
            "SELECT * FROM people WHERE first_name LIKE ? OR last_name LIKE ?";

    private final Gson gson;

    /**
     * Creates a new PersonRepository.
     *
     * @param connectionManager Database connection manager
     */
    public PersonRepository(ConnectionManager connectionManager) {
        super(connectionManager);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Initializes the repository by creating the people table if it doesn't exist.
     */
    @Override
    public void initialize() {
        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(CREATE_TABLE_SQL);
            LOGGER.info("People table initialized successfully");
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Failed to initialize people table: " + e.getMessage(),
                    e,
                    ErrorType.QUERY_EXECUTION_FAILED
            );
        }
    }

    /**
     * Finds a person by their ID.
     *
     * @param id Person ID
     * @return Optional containing the person if found, or empty if not found
     */
    @Override
    public Optional<Person> findById(Long id) {
        List<Person> results = executeQuery(
                FIND_BY_ID_SQL,
                stmt -> stmt.setLong(1, id),
                this::mapResultSetToPerson
        );

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Finds all people in the database.
     *
     * @return List of all people
     */
    @Override
    public List<Person> findAll() {
        return executeQuery(
                FIND_ALL_SQL,
                null,
                this::mapResultSetToPerson
        );
    }

    /**
     * Saves a person (insert if new, update if existing).
     *
     * @param person Person to save
     * @return Saved person with updated information
     */
    @Override
    public Person save(Person person) {
        boolean exists = existsById(person.getId());

        if (exists) {
            // Update existing person
            executeUpdate(
                    UPDATE_SQL,
                    stmt -> {
                        stmt.setString(1, person.getFirstName());
                        stmt.setString(2, person.getLastName());
                        stmt.setInt(3, person.getAge());
                        stmt.setString(4, person.getEmail());
                        stmt.setString(5, gson.toJson(person.getAdditionalData()));
                        stmt.setLong(6, person.getId());
                    }
            );
        } else {
            // Insert new person
            executeUpdate(
                    INSERT_SQL,
                    stmt -> {
                        stmt.setLong(1, person.getId());
                        stmt.setString(2, person.getFirstName());
                        stmt.setString(3, person.getLastName());
                        stmt.setInt(4, person.getAge());
                        stmt.setString(5, person.getEmail());
                        stmt.setString(6, gson.toJson(person.getAdditionalData()));
                    }
            );
        }

        return person;
    }

    /**
     * Deletes a person by their ID.
     *
     * @param id Person ID
     * @return true if person was deleted, false if not found
     */
    @Override
    public boolean deleteById(Long id) {
        int affected = executeUpdate(
                DELETE_SQL,
                stmt -> stmt.setLong(1, id)
        );

        return affected > 0;
    }

    /**
     * Checks if a person with the given ID exists.
     *
     * @param id Person ID
     * @return true if person exists, false otherwise
     */
    public boolean existsById(Long id) {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_BY_ID_SQL)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Failed to check if person exists: " + e.getMessage(),
                    e,
                    ErrorType.QUERY_EXECUTION_FAILED
            );
        }
    }

    /**
     * Finds people by first or last name (partial match).
     *
     * @param namePattern Name pattern to search for
     * @return List of matching people
     */
    public List<Person> findByName(String namePattern) {
        String pattern = "%" + namePattern + "%";

        return executeQuery(
                FIND_BY_NAME_SQL,
                stmt -> {
                    stmt.setString(1, pattern);
                    stmt.setString(2, pattern);
                },
                this::mapResultSetToPerson
        );
    }

    /**
     * Saves multiple people in a single batch operation.
     *
     * @param people List of people to save
     */
    public void saveAll(List<Person> people) {
        executeInTransaction(conn -> {
            try (PreparedStatement insertStmt = conn.prepareStatement(INSERT_SQL);
                 PreparedStatement updateStmt = conn.prepareStatement(UPDATE_SQL)) {

                for (Person person : people) {
                    boolean exists = existsById(person.getId());

                    if (exists) {
                        // Update existing person
                        updateStmt.setString(1, person.getFirstName());
                        updateStmt.setString(2, person.getLastName());
                        updateStmt.setInt(3, person.getAge());
                        updateStmt.setString(4, person.getEmail());
                        updateStmt.setString(5, gson.toJson(person.getAdditionalData()));
                        updateStmt.setLong(6, person.getId());
                        updateStmt.addBatch();
                    } else {
                        // Insert new person
                        insertStmt.setLong(1, person.getId());
                        insertStmt.setString(2, person.getFirstName());
                        insertStmt.setString(3, person.getLastName());
                        insertStmt.setInt(4, person.getAge());
                        insertStmt.setString(5, person.getEmail());
                        insertStmt.setString(6, gson.toJson(person.getAdditionalData()));
                        insertStmt.addBatch();
                    }
                }

                insertStmt.executeBatch();
                updateStmt.executeBatch();
                return null;
            }
        });
    }

    /**
     * Deletes all people from the database.
     *
     * @return Number of deleted records
     */
    public int deleteAll() {
        return executeUpdate("DELETE FROM people", null);
    }

    /**
     * Maps a database ResultSet row to a Person object.
     *
     * @param rs ResultSet positioned at the row to map
     * @return Mapped Person object
     * @throws SQLException if mapping fails
     */
    private Person mapResultSetToPerson(ResultSet rs) throws SQLException {
        long id = rs.getLong("person_id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        int age = rs.getInt("age");
        String email = rs.getString("email");
        String additionalDataJson = rs.getString("additional_data");

        // Parse additional data from JSON
        Type mapType = new TypeToken<java.util.Map<String, Object>>(){}.getType();
        java.util.Map<String, Object> additionalData = gson.fromJson(additionalDataJson, mapType);

        // Create and return Person object
        Person person = new Person(id, firstName, lastName, age, email);
        person.setAdditionalData(additionalData);
        return person;
    }

    /**
     * For backward compatibility - stores all people as a single JSON blob.
     * This recreates the original behavior but is not recommended for production use.
     *
     * @param people List of people to store
     * @deprecated Use saveAll() instead for better data management
     */
    @Deprecated
    public void storeAllAsJson(ArrayList<Person> people) {
        String serializedPeople = gson.toJson(people);

        executeInTransaction(conn -> {
            try (Statement stmt = conn.createStatement();
                 PreparedStatement insertStmt = conn.prepareStatement(
                         "INSERT INTO legacy_people (serialized_people) VALUES (?)")) {

                // Create legacy table if it doesn't exist
                stmt.execute(
                        "CREATE TABLE IF NOT EXISTS legacy_people (" +
                                "id SERIAL PRIMARY KEY, " +
                                "serialized_people TEXT)"
                );

                // Clear existing data
                stmt.execute("DELETE FROM legacy_people");

                // Insert new data
                insertStmt.setString(1, serializedPeople);
                insertStmt.executeUpdate();

                return null;
            }
        });
    }

    /**
     * For backward compatibility - retrieves all people from the JSON blob storage.
     *
     * @return List of people from the legacy storage
     * @deprecated Use findAll() instead for better data management
     */
    @Deprecated
    public ArrayList<Person> retrieveAllFromJson() {
        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Check if legacy table exists
            ResultSet tables = conn.getMetaData().getTables(
                    null, null, "legacy_people", null);

            if (!tables.next()) {
                return new ArrayList<>();
            }

            try (ResultSet rs = stmt.executeQuery(
                    "SELECT serialized_people FROM legacy_people LIMIT 1")) {

                if (rs.next()) {
                    String rawJson = rs.getString(1);
                    LOGGER.debug("Retrieved JSON data: {}", rawJson);

                    Type type = new TypeToken<ArrayList<Person>>(){}.getType();
                    return gson.fromJson(rawJson, type);
                }
            }

            return new ArrayList<>();
        } catch (SQLException e) {
            LOGGER.error("Error retrieving legacy people data", e);
            return new ArrayList<>();
        }
    }
}