package uz.myrafeeq.api.exception;

public final class UserNotFoundException extends MyRafeeqException {

  public UserNotFoundException(String message) {
    super("USER_NOT_FOUND", message);
  }
}
