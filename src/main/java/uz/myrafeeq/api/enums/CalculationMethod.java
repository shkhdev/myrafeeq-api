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
  SINGAPORE(com.batoulapps.adhan.CalculationMethod.SINGAPORE),
  MBOUZ(15.5, 15.5, 3);

  private final com.batoulapps.adhan.CalculationMethod adhanMethod;
  private final double customFajrAngle;
  private final double customIshaAngle;
  private final int maghribAdjustment;

  CalculationMethod(com.batoulapps.adhan.CalculationMethod adhanMethod) {
    this.adhanMethod = adhanMethod;
    this.customFajrAngle = 0;
    this.customIshaAngle = 0;
    this.maghribAdjustment = 0;
  }

  CalculationMethod(double fajrAngle, double ishaAngle, int maghribAdjustment) {
    this.adhanMethod = null;
    this.customFajrAngle = fajrAngle;
    this.customIshaAngle = ishaAngle;
    this.maghribAdjustment = maghribAdjustment;
  }

  public CalculationParameters getParameters() {
    if (adhanMethod != null) {
      return adhanMethod.getParameters();
    }
    CalculationParameters params = com.batoulapps.adhan.CalculationMethod.OTHER.getParameters();
    params.fajrAngle = customFajrAngle;
    params.ishaAngle = customIshaAngle;
    params.adjustments.maghrib = maghribAdjustment;
    return params;
  }
}
