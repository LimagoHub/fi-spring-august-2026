package de.fi.webapp.service.exception;

public class AlreadyExistsException extends RuntimeException{
    public AlreadyExistsException(final String message) {
        super(message);
    }
}
