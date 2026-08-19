package de.fi.webapp.service.exception;

public class SchweineServiceException extends RuntimeException {

    public SchweineServiceException(final String message) {
        super(message);
    }

    public SchweineServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
