package uz.myrafeeq.api.exception;

import lombok.Getter;

@Getter
public abstract sealed class MyRafeeqException extends RuntimeException
    permits InvalidAuthException,
        UserNotFoundException,
        PreferencesNotFoundException,
        CityNotFoundException,
        TrackingValidationException,
        OnboardingAlreadyCompletedException {

  private final String code;

  protected MyRafeeqException(String code, String message) {
    super(message);
    this.code = code;
  }
}
