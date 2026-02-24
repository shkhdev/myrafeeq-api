package uz.myrafeeq.api.enums;

import com.batoulapps.adhan.CalculationParameters;

public enum CalculationMethod {
  MWL(com.batoulapps.adhan.CalculationMethod.MUSLIM_WORLD_LEAGUE),
  ISNA(com.batoulapps.adhan.CalculationMethod.NORTH_AMERICA),
  EGYPT(com.batoulapps.adhan.CalculationMethod.EGYPTIAN),
  KARACHI(com.batoulapps.adhan.CalculationMethod.KARACHI),
  UMM_AL_QURA(com.batoulapps.adhan.CalculationMethod.UMM_AL_QURA),
  DUBAI(com.batoulapps.adhan.CalculationMethod.DUBAI),
  QATAR(com.batoulapps.adhan.CalculationMethod.QATAR),
  KUWAIT(com.batoulapps.adhan.CalculationMethod.KUWAIT),
  SINGAPORE(com.batoulapps.adhan.CalculationMethod.SINGAPORE);

  private final com.batoulapps.adhan.CalculationMethod adhanMethod;

  CalculationMethod(com.batoulapps.adhan.CalculationMethod adhanMethod) {
    this.adhanMethod = adhanMethod;
  }

  public CalculationParameters getParameters() {
    return adhanMethod.getParameters();
  }
}
