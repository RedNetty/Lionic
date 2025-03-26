package com.rednetty.lionic.sql.repository;

import com.rednetty.lionic.sql.ConnectionManager;
import com.rednetty.lionic.sql.exception.DatabaseException;
import com.rednetty.lionic.sql.exception.DatabaseException.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Abstract base class for database repositories.
 * Provides common database operations and transaction management.
 *
 * @param <T> The entity type this repository manages
 * @param <ID> The type of the entity's identifier
 */
public abstract class Repository<T, ID> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Repository.class);

    protected final ConnectionManager connectionManager;

    /**
     * Creates a new repository with the provided connection manager.
     *
     * @param connectionManager Database connection manager
     */
    protected Repository(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * Initializes the repository, creating necessary tables and indexes.
     */
    public abstract void initialize();

    /**
     * Finds an entity by its ID.
     *
     * @param id Entity identifier
     * @return Optional containing the entity if found, or empty if not found
     */
    public abstract Optional<T> findById(ID id);

    /**
     * Finds all entities managed by this repository.
     *
     * @return List of all entities
     */
    public abstract List<T> findAll();

    /**
     * Saves an entity (insert if new, update if existing).
     *
     * @param entity Entity to save
     * @return Saved entity (potentially with updated ID)
     */
    public abstract T save(T entity);

    /**
     * Deletes an entity by its ID.
     *
     * @param id Entity identifier
     * @return true if entity was deleted, false if not found
     */
    public abstract boolean deleteById(ID id);

    /**
     * Executes a SQL query and maps the result to a list of entities.
     *
     * @param sql SQL query string
     * @param paramSetter Function to set query parameters
     * @param mapper Function to map ResultSet to entity
     * @return List of mapped entities
     */
    protected <R> List<R> executeQuery(
            String sql,
            ParameterSetter paramSetter,
            ResultSetMapper<R> mapper) {

        List<R> results = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (paramSetter != null) {
                paramSetter.setParameters(stmt);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
            }

            return results;
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Failed to execute query: " + e.getMessage(),
                    e,
                    ErrorType.QUERY_EXECUTION_FAILED
            );
        }
    }

    /**
     * Executes a SQL update (INSERT, UPDATE, DELETE) statement.
     *
     * @param sql SQL update statement
     * @param paramSetter Function to set statement parameters
     * @return Number of affected rows
     */
    protected int executeUpdate(String sql, ParameterSetter paramSetter) {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (paramSetter != null) {
                paramSetter.setParameters(stmt);
            }

            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Failed to execute update: " + e.getMessage(),
                    e,
                    ErrorType.QUERY_EXECUTION_FAILED
            );
        }
    }

    /**
     * Executes an insert statement and returns the generated keys.
     *
     * @param sql SQL insert statement
     * @param paramSetter Function to set statement parameters
     * @param keyMapper Function to map generated keys to result type
     * @return Result from the key mapper
     */
    protected <R> R executeInsert(
            String sql,
            ParameterSetter paramSetter,
            Function<ResultSet, R> keyMapper) {

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            if (paramSetter != null) {
                paramSetter.setParameters(stmt);
            }

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keyMapper.apply(keys);
                } else {
                    throw new DatabaseException(
                            "No generated keys returned from insert",
                            ErrorType.QUERY_EXECUTION_FAILED
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Failed to execute insert: " + e.getMessage(),
                    e,
                    ErrorType.QUERY_EXECUTION_FAILED
            );
        }
    }

    /**
     * Executes a SQL batch update.
     *
     * @param sql SQL statement
     * @param batchSetter Function to set batch parameters
     * @return Array of affected row counts
     */
    protected int[] executeBatch(String sql, BatchParameterSetter batchSetter) {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            batchSetter.setParameters(stmt);
            return stmt.executeBatch();
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Failed to execute batch update: " + e.getMessage(),
                    e,
                    ErrorType.QUERY_EXECUTION_FAILED
            );
        }
    }

    /**
     * Executes multiple operations in a single transaction.
     *
     * @param operation Function containing the operations to execute in transaction
     * @return Result of the transaction operation
     */
    protected <R> R executeInTransaction(TransactionOperation<R> operation) {
        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = connectionManager.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            R result = operation.execute(conn);

            conn.commit();
            return result;
        } catch (Exception e) {
            // Rollback transaction on error
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.warn("Transaction rolled back due to error: {}", e.getMessage());
                } catch (SQLException rollbackEx) {
                    LOGGER.error("Failed to rollback transaction", rollbackEx);
                }
            }

            if (e instanceof DatabaseException) {
                throw (DatabaseException) e;
            } else {
                throw new DatabaseException(
                        "Transaction failed: " + e.getMessage(),
                        e,
                        ErrorType.TRANSACTION_FAILED
                );
            }
        } finally {
            // Restore original auto-commit state and close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Error closing connection", e);
                }
            }
        }
    }

    /**
     * Functional interface for setting parameters on a PreparedStatement.
     */
    @FunctionalInterface
    protected interface ParameterSetter {
        void setParameters(PreparedStatement stmt) throws SQLException;
    }

    /**
     * Functional interface for setting batch parameters on a PreparedStatement.
     */
    @FunctionalInterface
    protected interface BatchParameterSetter {
        void setParameters(PreparedStatement stmt) throws SQLException;
    }

    /**
     * Functional interface for mapping a ResultSet row to an entity.
     */
    @FunctionalInterface
    protected interface ResultSetMapper<R> {
        R map(ResultSet rs) throws SQLException;
    }

    /**
     * Functional interface for transaction operations.
     */
    @FunctionalInterface
    protected interface TransactionOperation<R> {
        R execute(Connection conn) throws SQLException;
    }
}