package com.rednetty.lionic.sql.exception;

/**
 * Custom exception to represent database-related errors.
 * This exception provides a consistent way to handle and report
 * database operation failures throughout the application.
 */
public class DatabaseException extends RuntimeException {

    /**
     * Exception type categorizing the nature of the database error.
     */
    private final ErrorType errorType;

    /**
     * Enumeration of possible database error types for better error categorization.
     */
    public enum ErrorType {
        CONNECTION_FAILED,
        QUERY_EXECUTION_FAILED,
        DATA_ACCESS_FAILED,
        CONFIGURATION_ERROR,
        TRANSACTION_FAILED,
        UNKNOWN_ERROR
    }

    /**
     * Creates a new DatabaseException with a message and error type.
     *
     * @param message Detailed error message
     * @param errorType Type of database error
     */
    public DatabaseException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    /**
     * Creates a new DatabaseException with a message, cause, and error type.
     *
     * @param message Detailed error message
     * @param cause Original exception that caused this error
     * @param errorType Type of database error
     */
    public DatabaseException(String message, Throwable cause, ErrorType errorType) {
        super(message, cause);
        this.errorType = errorType;
    }

    /**
     * @return The error type of this exception
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * Creates a formatted error message with error type information.
     *
     * @return Enhanced error message with type information
     */
    @Override
    public String getMessage() {
        return String.format("[%s] %s", errorType, super.getMessage());
    }
}