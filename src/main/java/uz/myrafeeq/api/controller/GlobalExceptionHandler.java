package uz.myrafeeq.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uz.myrafeeq.api.dto.response.ErrorResponse;
import uz.myrafeeq.api.exception.CityNotFoundException;
import uz.myrafeeq.api.exception.InvalidAuthException;
import uz.myrafeeq.api.exception.MyRafeeqException;
import uz.myrafeeq.api.exception.OnboardingAlreadyCompletedException;
import uz.myrafeeq.api.exception.PreferencesNotFoundException;
import uz.myrafeeq.api.exception.TrackingValidationException;
import uz.myrafeeq.api.exception.UserNotFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MyRafeeqException.class)
  public ResponseEntity<ErrorResponse> handleMyRafeeqException(MyRafeeqException ex) {
    HttpStatus status =
        switch (ex) {
          case InvalidAuthException _ -> HttpStatus.UNAUTHORIZED;
          case UserNotFoundException _ -> HttpStatus.NOT_FOUND;
          case PreferencesNotFoundException _ -> HttpStatus.NOT_FOUND;
          case CityNotFoundException _ -> HttpStatus.NOT_FOUND;
          case TrackingValidationException _ -> HttpStatus.BAD_REQUEST;
          case OnboardingAlreadyCompletedException _ -> HttpStatus.CONFLICT;
        };

    if (status.is5xxServerError()) {
      log.error("MyRafeeq error [{}]: {}", ex.getCode(), ex.getMessage());
    } else {
      log.warn("MyRafeeq error [{}]: {}", ex.getCode(), ex.getMessage());
    }

    return ResponseEntity.status(status).body(ErrorResponse.of(ex.getCode(), ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .orElse("Validation failed");

    log.warn("Validation error: {}", message);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.of("VALIDATION_ERROR", message));
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ErrorResponse> handleMethodValidation(HandlerMethodValidationException ex) {
    String message =
        ex.getAllErrors().stream()
            .findFirst()
            .map(error -> error.getDefaultMessage())
            .orElse("Validation failed");

    log.warn("Parameter validation error: {}", message);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.of("VALIDATION_ERROR", message));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex) {
    log.warn("Invalid request body: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.of("INVALID_REQUEST_BODY", "Malformed or unreadable request body"));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    log.warn("Type mismatch for parameter '{}': {}", ex.getName(), ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ErrorResponse.of("INVALID_PARAMETER", "Invalid value for parameter: " + ex.getName()));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex) {
    log.warn("Method not supported: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(
            ErrorResponse.of("METHOD_NOT_ALLOWED", "HTTP method not supported: " + ex.getMethod()));
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex) {
    log.warn("Unsupported media type: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        .body(
            ErrorResponse.of(
                "UNSUPPORTED_MEDIA_TYPE", "Content-Type not supported: " + ex.getContentType()));
  }

  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  public ResponseEntity<ErrorResponse> handleMediaTypeNotAcceptable(
      HttpMediaTypeNotAcceptableException ex) {
    log.warn("Not acceptable media type: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
        .body(ErrorResponse.of("NOT_ACCEPTABLE", "Requested media type is not supported"));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
    log.warn("Resource not found: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.of("RESOURCE_NOT_FOUND", "Requested resource not found"));
  }

  @ExceptionHandler(OptimisticLockingFailureException.class)
  public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(
      OptimisticLockingFailureException ex) {
    log.warn("Optimistic locking conflict: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            ErrorResponse.of(
                "CONFLICT", "The resource was modified by another request. Please retry."));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
      DataIntegrityViolationException ex) {
    log.warn("Data integrity violation: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponse.of("DATA_CONFLICT", "Operation conflicts with existing data"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
    log.error("Unexpected error: {}", ex.getMessage(), ex);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred"));
  }
}
