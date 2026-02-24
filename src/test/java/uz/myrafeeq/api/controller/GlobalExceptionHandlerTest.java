package uz.myrafeeq.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.myrafeeq.api.dto.response.ErrorResponse;
import uz.myrafeeq.api.exception.InvalidAuthException;
import uz.myrafeeq.api.exception.OnboardingAlreadyCompletedException;
import uz.myrafeeq.api.exception.TrackingValidationException;
import uz.myrafeeq.api.exception.UserNotFoundException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleInvalidAuthException() {
    InvalidAuthException ex = new InvalidAuthException("Invalid token");

    ResponseEntity<ErrorResponse> response = handler.handleMyRafeeqException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("INVALID_AUTH");
    assertThat(response.getBody().error().message()).isEqualTo("Invalid token");
  }

  @Test
  void handleUserNotFoundException() {
    UserNotFoundException ex = new UserNotFoundException("User not found: 123");

    ResponseEntity<ErrorResponse> response = handler.handleMyRafeeqException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("USER_NOT_FOUND");
    assertThat(response.getBody().error().message()).isEqualTo("User not found: 123");
  }

  @Test
  void handleTrackingValidationException() {
    TrackingValidationException ex = new TrackingValidationException("Invalid prayer name");

    ResponseEntity<ErrorResponse> response = handler.handleMyRafeeqException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("TRACKING_VALIDATION_ERROR");
    assertThat(response.getBody().error().message()).isEqualTo("Invalid prayer name");
  }

  @Test
  void handleOnboardingAlreadyCompleted() {
    OnboardingAlreadyCompletedException ex =
        new OnboardingAlreadyCompletedException("Onboarding already completed for user: 123");

    ResponseEntity<ErrorResponse> response = handler.handleMyRafeeqException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("ONBOARDING_ALREADY_COMPLETED");
    assertThat(response.getBody().error().message())
        .isEqualTo("Onboarding already completed for user: 123");
  }

  @Test
  void handleUnexpectedException() {
    Exception ex = new RuntimeException("Something went wrong");

    ResponseEntity<ErrorResponse> response = handler.handleUnexpectedException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("INTERNAL_ERROR");
    assertThat(response.getBody().error().message()).isEqualTo("An unexpected error occurred");
  }

  @Test
  void handleOptimisticLocking() {
    OptimisticLockingFailureException ex =
        new OptimisticLockingFailureException("Row was updated by another transaction");

    ResponseEntity<ErrorResponse> response = handler.handleOptimisticLocking(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("CONCURRENT_MODIFICATION");
    assertThat(response.getBody().error().message())
        .isEqualTo("Concurrent modification detected, please retry");
  }
}
