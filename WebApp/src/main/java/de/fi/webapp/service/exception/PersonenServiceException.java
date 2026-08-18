package de.fi.webapp.service.exception;

public class PersonenServiceException extends Exception {
    public PersonenServiceException(final String message) {
        super(message);
    }

    public PersonenServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
