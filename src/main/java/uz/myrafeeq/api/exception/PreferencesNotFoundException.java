package uz.myrafeeq.api.exception;

public final class PreferencesNotFoundException extends MyRafeeqException {

  public PreferencesNotFoundException(String message) {
    super("PREFERENCES_NOT_FOUND", message);
  }
}
