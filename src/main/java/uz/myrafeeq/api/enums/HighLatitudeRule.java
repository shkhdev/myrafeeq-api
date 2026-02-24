package uz.myrafeeq.api.enums;

public enum HighLatitudeRule {
  MIDDLE_OF_NIGHT(com.batoulapps.adhan.HighLatitudeRule.MIDDLE_OF_THE_NIGHT),
  ONE_SEVENTH(com.batoulapps.adhan.HighLatitudeRule.SEVENTH_OF_THE_NIGHT),
  ANGLE_BASED(com.batoulapps.adhan.HighLatitudeRule.TWILIGHT_ANGLE);

  private final com.batoulapps.adhan.HighLatitudeRule adhanRule;

  HighLatitudeRule(com.batoulapps.adhan.HighLatitudeRule adhanRule) {
    this.adhanRule = adhanRule;
  }

  public com.batoulapps.adhan.HighLatitudeRule toAdhan() {
    return adhanRule;
  }
}
