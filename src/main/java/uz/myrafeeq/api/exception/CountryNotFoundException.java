package uz.myrafeeq.api.exception;

public final class CountryNotFoundException extends MyRafeeqException {

  public CountryNotFoundException(String message) {
    super("COUNTRY_NOT_FOUND", message);
  }
}
