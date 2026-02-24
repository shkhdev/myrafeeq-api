package uz.myrafeeq.api.exception;

public final class CityNotFoundException extends MyRafeeqException {

  public CityNotFoundException(String message) {
    super("CITY_NOT_FOUND", message);
  }
}
