package uz.myrafeeq.api.exception;

public final class TrackingValidationException extends MyRafeeqException {

  public TrackingValidationException(String message) {
    super("TRACKING_VALIDATION_ERROR", message);
  }
}
