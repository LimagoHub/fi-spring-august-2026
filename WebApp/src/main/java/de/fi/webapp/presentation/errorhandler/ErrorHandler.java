package de.fi.webapp.presentation.errorhandler;

import de.fi.webapp.presentation.exception.IdMismatchException;
import de.fi.webapp.service.exception.AlreadyExistsException;
import de.fi.webapp.service.exception.NotFoundException;
import de.fi.webapp.service.exception.PersonenServiceException;
import de.fi.webapp.service.exception.SchweineServiceException;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex, final HttpHeaders headers, final HttpStatusCode status, final WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(x -> x.getField() + ":" + x.getDefaultMessage())
                .collect(Collectors.toList());
        body.put("errors", errors);

        // WICHTIG !!!!!!
        logger.error("Upps", ex);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(PersonenServiceException.class)
    public ResponseEntity<Object> handlePersonenServiceException(PersonenServiceException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        body.put("type", ex.getClass().getSimpleName());// Achtung security
        logger.error("Upps", ex);
        return ResponseEntity.internalServerError().body(body);
    }

    @ExceptionHandler(SchweineServiceException.class)
    public ResponseEntity<Object> handleSchweineServiceException(SchweineServiceException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        body.put("type", ex.getClass().getSimpleName());// Achtung security
        logger.error("Upps", ex);
        return ResponseEntity.internalServerError().body(body);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<Object> handleAlreadyExistsException(AlreadyExistsException ex, WebRequest request) {
        logger.error("Upps", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException ex, WebRequest request) {
        logger.error("Upps", ex);
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(IdMismatchException.class)
    public ResponseEntity<Object> handleIdMismatchException(IdMismatchException ex, WebRequest request) {
        logger.error("Upps", ex);
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

}
