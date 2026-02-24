package uz.myrafeeq.api.exception;

public final class OnboardingAlreadyCompletedException extends MyRafeeqException {

  public OnboardingAlreadyCompletedException(String message) {
    super("ONBOARDING_ALREADY_COMPLETED", message);
  }
}
