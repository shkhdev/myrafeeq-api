package uz.myrafeeq.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.myrafeeq.api.dto.response.ErrorResponse;
import uz.myrafeeq.api.exception.CityNotFoundException;
import uz.myrafeeq.api.exception.InvalidAuthException;
import uz.myrafeeq.api.exception.OnboardingAlreadyCompletedException;
import uz.myrafeeq.api.exception.PreferencesNotFoundException;
import uz.myrafeeq.api.exception.TrackingValidationException;
import uz.myrafeeq.api.exception.UserNotFoundException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void should_return401_when_invalidAuthException() {
    ResponseEntity<ErrorResponse> response =
        handler.handleMyRafeeqException(new InvalidAuthException("Bad auth"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("INVALID_AUTH");
    assertThat(response.getBody().error().message()).isEqualTo("Bad auth");
  }

  @Test
  void should_return404_when_userNotFoundException() {
    ResponseEntity<ErrorResponse> response =
        handler.handleMyRafeeqException(new UserNotFoundException("User not found"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("USER_NOT_FOUND");
  }

  @Test
  void should_return404_when_preferencesNotFoundException() {
    ResponseEntity<ErrorResponse> response =
        handler.handleMyRafeeqException(new PreferencesNotFoundException("Preferences not found"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("PREFERENCES_NOT_FOUND");
  }

  @Test
  void should_return404_when_cityNotFoundException() {
    ResponseEntity<ErrorResponse> response =
        handler.handleMyRafeeqException(new CityNotFoundException("City not found"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("CITY_NOT_FOUND");
  }

  @Test
  void should_return400_when_trackingValidationException() {
    ResponseEntity<ErrorResponse> response =
        handler.handleMyRafeeqException(new TrackingValidationException("Invalid date"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("TRACKING_VALIDATION_ERROR");
  }

  @Test
  void should_return409_when_onboardingAlreadyCompletedException() {
    ResponseEntity<ErrorResponse> response =
        handler.handleMyRafeeqException(
            new OnboardingAlreadyCompletedException("Already completed"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("ONBOARDING_ALREADY_COMPLETED");
  }

  @Test
  void should_return500_when_unexpectedException() {
    ResponseEntity<ErrorResponse> response =
        handler.handleUnexpectedException(new RuntimeException("Something broke"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("INTERNAL_ERROR");
  }

  @Test
  void should_return409_when_optimisticLockingFailure() {
    ResponseEntity<ErrorResponse> response =
        handler.handleOptimisticLockingFailure(
            new org.springframework.dao.OptimisticLockingFailureException("Conflict"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("CONFLICT");
  }

  @Test
  void should_return405_when_methodNotSupported() {
    ResponseEntity<ErrorResponse> response =
        handler.handleMethodNotSupported(
            new org.springframework.web.HttpRequestMethodNotSupportedException("DELETE"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("METHOD_NOT_ALLOWED");
  }
}
