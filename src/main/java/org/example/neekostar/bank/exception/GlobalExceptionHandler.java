package org.example.neekostar.bank.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @Autowired
    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Map<String, Object>> handleValidation(BindException ex,
                                                                Locale locale,
                                                                HttpServletRequest request) {
        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(err -> messageSource.getMessage(err, locale))
                .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, "Errors in request data",
                "Invalid data", request, Map.of("errors", errors));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex,
                                                              HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage(), request);
    }

    @ExceptionHandler({UnauthorizedException.class, BadCredentialsException.class})
    public ResponseEntity<Map<String, Object>> handleUnauthorized(RuntimeException ex,
                                                                  HttpServletRequest request) {
        String msg = (ex instanceof BadCredentialsException)
                ? "Invalid credentials provided"
                : ex.getMessage();

        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized access", msg, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(AccessDeniedException ex,
                                                               HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Access denied", ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(DuplicateResourceException ex,
                                                              HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Конфликт данных", ex.getMessage(), request);
    }

    @ExceptionHandler({IllegalArgumentException.class, InvalidArgumentException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException ex,
                                                                HttpServletRequest request) {
        String msg = (ex instanceof MethodArgumentTypeMismatchException mismatch)
                ? "Invalid parameter type: " + mismatch.getName()
                : ex.getMessage();

        return buildResponse(HttpStatus.BAD_REQUEST, "Data conflict", msg, request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDatabaseError(DataAccessException ex,
                                                                   HttpServletRequest request) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE,
                "Database Error",
                "Database operation failed: " + ex.getMessage(),
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex,
                                                         HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                ex.getMessage(), request);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status,
                                                              String error,
                                                              String message,
                                                              HttpServletRequest request) {
        return buildResponse(status, error, message, request, Map.of());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status,
                                                              String error,
                                                              String message,
                                                              HttpServletRequest request,
                                                              Map<String, ?> extra) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", request.getRequestURI());
        body.put("method", request.getMethod());
        body.putAll(extra);

        return ResponseEntity.status(status).body(body);
    }
}
