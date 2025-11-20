//package com.sims.common.exceptions.handler;
//
//
//import com.sims.common.exceptions.*;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.logging.LogLevel;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
//
//import java.util.Date;
//
///**
// * Base Exception Handler for all SIMS microservices
// * Contains common exception handling logic
// * Each service should extend this and add service-specific handlers
// *
// * @author LastCoderBoy
// * @since 2025-01-17
// */
//@Slf4j
//public abstract class BaseExceptionHandler {
//    protected static final String LOG_PREFIX = "[EXCEPTION] ";
//
//    protected ResponseEntity<ErrorResponse> handleException(Exception ex, HttpStatus status, String logMessage) {
//        log.warn(logMessage, ex.getMessage());
//        ErrorResponse errorObject = new ErrorResponse(
//                status.value(),
//                ex.getClass().getSimpleName(),
//                ex.getMessage(),
//                new Date()
//        );
//        return new ResponseEntity<>(errorObject, status);
//    }
//
//    protected ResponseEntity<ErrorResponse> handleException(Exception ex, HttpStatus status, LogLevel logLevel, String logMessage) {
//        if (logLevel == LogLevel.ERROR) {
//            log.error(logMessage, ex.getMessage());
//        } else {
//            log.warn(logMessage, ex.getMessage());
//        }
//        ErrorResponse errorObject = new ErrorResponse(
//                status.value(),
//                ex.getClass().getSimpleName(),
//                ex.getMessage(),
//                new Date()
//        );
//        return new ResponseEntity<>(errorObject, status);
//    }
//
//    // ========================================
//    // COMMON EXCEPTION HANDLERS
//    // ========================================
//
//    /**
//     * Handle ValidationException (400 Bad Request)
//     */
//    @ExceptionHandler(ValidationException.class)
//    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
//        return handleException(ex, HttpStatus.BAD_REQUEST, "{}Validation error: {}");
//    }
//
//    /**
//     * Handle @Valid annotation errors (400 Bad Request)
//     */
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
//        log.warn("{}Validation failed: {}", LOG_PREFIX, ex.getMessage());
//        StringBuilder errorMessage = new StringBuilder("Validation failed: ");
//        ex.getBindingResult().getFieldErrors().forEach(error ->
//                errorMessage.append(String.format("[%s: %s] ", error.getField(), error.getDefaultMessage()))
//        );
//        ErrorResponse errorObject = new ErrorResponse(
//                HttpStatus.BAD_REQUEST.value(),
//                "ValidationError",
//                errorMessage.toString(),
//                new Date()
//        );
//        return new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
//    }
//
//    /**
//     * Handle enum conversion failures (400 Bad Request)
//     */
//    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
//    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
//        log.error("{}Type mismatch error: {}", LOG_PREFIX, ex.getMessage());
//        String error = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
//        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
//            Object[] enumConstants = ex.getRequiredType().getEnumConstants();
//            error += ". Valid values: " + java.util.Arrays.toString(enumConstants);
//        }
//        ErrorResponse errorObject = new ErrorResponse(
//                HttpStatus.BAD_REQUEST.value(),
//                "TypeMismatchError",
//                error,
//                new Date()
//        );
//        return new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
//    }
//
//    /**
//     * Handle ResourceNotFoundException (404 Not Found)
//     */
//    @ExceptionHandler(ResourceNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
//        return handleException(ex, HttpStatus.NOT_FOUND, "{}Resource not found: {}");
//    }
//
//    /**
//     * Handle InvalidTokenException (401 Unauthorized)
//     */
//    @ExceptionHandler(InvalidTokenException.class)
//    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex) {
//        log.warn("{}Invalid token: {}", LOG_PREFIX, ex.getMessage());
//        return handleException(ex, HttpStatus.UNAUTHORIZED, "Invalid Token Provided");
//    }
//
//    /**
//     * Handle ServiceException (500 Internal Server Error)
//     */
//    @ExceptionHandler({DatabaseException.class, ServiceException.class})
//    public ResponseEntity<ErrorResponse> handleServerExceptions(Exception ex) {
//        return handleException(ex, HttpStatus.INTERNAL_SERVER_ERROR, LogLevel.ERROR, "{}Server error: {}");
//    }
//
//    /**
//     * Handle generic Exception (500 Internal Server Error)
//     */
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
//        log.error("{}Unexpected error: {}", LOG_PREFIX, ex.getMessage(), ex);
//        ErrorResponse errorObject = new ErrorResponse(
//                HttpStatus.INTERNAL_SERVER_ERROR.value(),
//                "InternalServerError",
//                "An unexpected error occurred. Please contact support.",
//                new Date()
//        );
//        return new ResponseEntity<>(errorObject, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//}
