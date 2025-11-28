package edu.dosw.exception;


public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * Constructs an AuthenticationException with the specified detail message and cause
     *
     * @param message The detail message explaining the authentication failure
     * @param cause The underlying cause of the exception
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}