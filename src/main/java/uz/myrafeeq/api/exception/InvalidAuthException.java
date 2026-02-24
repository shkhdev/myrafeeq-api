package uz.myrafeeq.api.exception;

public final class InvalidAuthException extends MyRafeeqException {

  public InvalidAuthException(String message) {
    super("INVALID_AUTH", message);
  }
}
