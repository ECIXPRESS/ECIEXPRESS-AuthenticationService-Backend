package edu.dosw.exception;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }

    /**
     * Constructs a ServiceUnavailableException with the specified detail message and cause
     *
     * @param message The detail message explaining the service unavailability
     * @param cause The underlying cause of the exception
     */
    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}