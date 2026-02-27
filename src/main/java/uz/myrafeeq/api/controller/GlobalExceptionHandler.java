package uz.myrafeeq.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
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
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uz.myrafeeq.api.dto.response.ErrorResponse;
import uz.myrafeeq.api.exception.CityNotFoundException;
import uz.myrafeeq.api.exception.CountryNotFoundException;
import uz.myrafeeq.api.exception.InvalidAuthException;
import uz.myrafeeq.api.exception.MyRafeeqException;
import uz.myrafeeq.api.exception.OnboardingAlreadyCompletedException;
import uz.myrafeeq.api.exception.PreferencesNotFoundException;
import uz.myrafeeq.api.exception.RequestValidationException;
import uz.myrafeeq.api.exception.TrackingValidationException;
import uz.myrafeeq.api.exception.UserNotFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MyRafeeqException.class)
  public ResponseEntity<ErrorResponse> handleMyRafeeqException(
      MyRafeeqException ex, HttpServletRequest request) {
    HttpStatus status =
        switch (ex) {
          case InvalidAuthException _ -> HttpStatus.UNAUTHORIZED;
          case UserNotFoundException _ -> HttpStatus.NOT_FOUND;
          case PreferencesNotFoundException _ -> HttpStatus.NOT_FOUND;
          case CityNotFoundException _ -> HttpStatus.NOT_FOUND;
          case CountryNotFoundException _ -> HttpStatus.NOT_FOUND;
          case TrackingValidationException _ -> HttpStatus.BAD_REQUEST;
          case OnboardingAlreadyCompletedException _ -> HttpStatus.CONFLICT;
          case RequestValidationException _ -> HttpStatus.BAD_REQUEST;
        };

    if (status.is5xxServerError()) {
      log.error("MyRafeeq error [{}]: {}", ex.getCode(), ex.getMessage());
    } else {
      log.warn("MyRafeeq error [{}]: {}", ex.getCode(), ex.getMessage());
    }

    return ResponseEntity.status(status).body(error(ex.getCode(), ex.getMessage(), request));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<ErrorResponse.FieldError> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
            .toList();

    log.warn("Validation error: {} field(s) invalid", fieldErrors.size());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.ofValidation(fieldErrors, Instant.now(), request.getRequestURI()));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex, HttpServletRequest request) {
    log.warn("Missing required parameter '{}': {}", ex.getParameterName(), ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            error(
                "MISSING_PARAMETER",
                "Required parameter '" + ex.getParameterName() + "' is missing",
                request));
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ErrorResponse> handleMethodValidation(
      HandlerMethodValidationException ex, HttpServletRequest request) {
    String message =
        ex.getAllErrors().stream()
            .findFirst()
            .map(error -> error.getDefaultMessage())
            .orElse("Validation failed");

    log.warn("Parameter validation error: {}", message);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(error("VALIDATION_ERROR", message, request));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    log.warn("Invalid request body: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(error("INVALID_REQUEST_BODY", "Malformed or unreadable request body", request));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    log.warn("Type mismatch for parameter '{}': {}", ex.getName(), ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(error("INVALID_PARAMETER", "Invalid value for parameter: " + ex.getName(), request));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    log.warn("Method not supported: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(error("METHOD_NOT_ALLOWED", "HTTP method not supported: " + ex.getMethod(), request));
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
    log.warn("Unsupported media type: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        .body(
            error(
                "UNSUPPORTED_MEDIA_TYPE",
                "Content-Type not supported: " + ex.getContentType(),
                request));
  }

  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  public ResponseEntity<ErrorResponse> handleMediaTypeNotAcceptable(
      HttpMediaTypeNotAcceptableException ex, HttpServletRequest request) {
    log.warn("Not acceptable media type: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
        .body(error("NOT_ACCEPTABLE", "Requested media type is not supported", request));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFound(
      NoResourceFoundException ex, HttpServletRequest request) {
    log.warn("Resource not found: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(error("RESOURCE_NOT_FOUND", "Requested resource not found", request));
  }

  @ExceptionHandler(OptimisticLockingFailureException.class)
  public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(
      OptimisticLockingFailureException ex, HttpServletRequest request) {
    log.warn("Optimistic locking conflict: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            error(
                "CONFLICT",
                "The resource was modified by another request. Please retry.",
                request));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
      DataIntegrityViolationException ex, HttpServletRequest request) {
    log.warn("Data integrity violation: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(error("DATA_CONFLICT", "Operation conflicts with existing data", request));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(
      Exception ex, HttpServletRequest request) {
    log.error("Unexpected error: {}", ex.getMessage(), ex);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(error("INTERNAL_ERROR", "An unexpected error occurred", request));
  }

  private ErrorResponse error(String code, String message, HttpServletRequest request) {
    return ErrorResponse.of(code, message, Instant.now(), request.getRequestURI());
  }
}
