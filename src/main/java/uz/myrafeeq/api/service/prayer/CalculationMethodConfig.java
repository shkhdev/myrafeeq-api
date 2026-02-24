package uz.myrafeeq.api.service.prayer;

import java.util.Map;
import uz.myrafeeq.api.enums.CalculationMethod;

/**
 * Configuration for prayer calculation methods. Stores the fajr and isha sun angles for each
 * method.
 */
public record CalculationMethodConfig(double fajrAngle, double ishaAngle) {

  private static final Map<CalculationMethod, CalculationMethodConfig> METHODS =
      Map.ofEntries(
          Map.entry(CalculationMethod.MWL, new CalculationMethodConfig(18.0, 17.0)),
          Map.entry(CalculationMethod.ISNA, new CalculationMethodConfig(15.0, 15.0)),
          Map.entry(CalculationMethod.EGYPT, new CalculationMethodConfig(19.5, 17.5)),
          Map.entry(CalculationMethod.KARACHI, new CalculationMethodConfig(18.0, 18.0)),
          Map.entry(CalculationMethod.UMM_AL_QURA, new CalculationMethodConfig(18.5, 0.0)),
          Map.entry(CalculationMethod.DUBAI, new CalculationMethodConfig(18.2, 18.2)),
          Map.entry(CalculationMethod.QATAR, new CalculationMethodConfig(18.0, 0.0)),
          Map.entry(CalculationMethod.KUWAIT, new CalculationMethodConfig(18.0, 17.5)),
          Map.entry(CalculationMethod.TURKEY, new CalculationMethodConfig(18.0, 17.0)),
          Map.entry(CalculationMethod.TEHRAN, new CalculationMethodConfig(17.7, 14.0)),
          Map.entry(CalculationMethod.JAKIM, new CalculationMethodConfig(20.0, 18.0)),
          Map.entry(CalculationMethod.KEMENAG, new CalculationMethodConfig(20.0, 18.0)),
          Map.entry(CalculationMethod.SINGAPORE, new CalculationMethodConfig(20.0, 18.0)));

  public static CalculationMethodConfig forMethod(CalculationMethod method) {
    CalculationMethodConfig config = METHODS.get(method);
    if (config == null) {
      return METHODS.get(CalculationMethod.MWL);
    }
    return config;
  }

  /**
   * Returns the isha time mode. For Umm al-Qura and Qatar, isha is a fixed offset after Maghrib
   * rather than an angle.
   */
  public boolean isIshaFixedOffset() {
    return ishaAngle == 0.0;
  }

  /**
   * Returns the fixed isha offset in minutes for methods that use it. Umm al-Qura uses 90 minutes
   * (120 during Ramadan). Qatar uses 90 minutes.
   */
  public int ishaOffsetMinutes() {
    return 90;
  }
}
