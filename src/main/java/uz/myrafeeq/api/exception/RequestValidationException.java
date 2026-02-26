package uz.myrafeeq.api.exception;

public final class RequestValidationException extends MyRafeeqException {

  public RequestValidationException(String message) {
    super("VALIDATION_ERROR", message);
  }
}
