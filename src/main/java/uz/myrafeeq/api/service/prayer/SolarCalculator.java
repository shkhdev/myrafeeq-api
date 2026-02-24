package uz.myrafeeq.api.service.prayer;

import java.time.LocalDate;

/**
 * Utility class for astronomical calculations used in prayer time computation. Based on algorithms
 * from NOAA Solar Calculator and the U.S. Naval Observatory.
 */
public final class SolarCalculator {

  private SolarCalculator() {}

  /** Converts a Gregorian date to Julian Date. */
  public static double julianDate(int year, int month, int day) {
    if (month <= 2) {
      year -= 1;
      month += 12;
    }
    int a = year / 100;
    int b = 2 - a + a / 4;
    return Math.floor(365.25 * (year + 4716))
        + Math.floor(30.6001 * (month + 1))
        + day
        + b
        - 1524.5;
  }

  /** Converts a LocalDate to Julian Date. */
  public static double julianDate(LocalDate date) {
    return julianDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
  }

  /** Computes Julian Century from Julian Date. */
  public static double julianCentury(double jd) {
    return (jd - 2451545.0) / 36525.0;
  }

  /** Computes the Sun's geometric mean longitude (in degrees). */
  public static double sunMeanLongitude(double t) {
    double l0 = 280.46646 + t * (36000.76983 + 0.0003032 * t);
    return l0 % 360.0;
  }

  /** Computes the Sun's mean anomaly (in degrees). */
  public static double sunMeanAnomaly(double t) {
    return 357.52911 + t * (35999.05029 - 0.0001537 * t);
  }

  /** Computes the eccentricity of Earth's orbit. */
  public static double earthOrbitEccentricity(double t) {
    return 0.016708634 - t * (0.000042037 + 0.0000001267 * t);
  }

  /** Computes the Sun's equation of center (in degrees). */
  public static double sunEquationOfCenter(double t) {
    double m = Math.toRadians(sunMeanAnomaly(t));
    return Math.sin(m) * (1.914602 - t * (0.004817 + 0.000014 * t))
        + Math.sin(2 * m) * (0.019993 - 0.000101 * t)
        + Math.sin(3 * m) * 0.000289;
  }

  /** Computes the Sun's true longitude (in degrees). */
  public static double sunTrueLongitude(double t) {
    return sunMeanLongitude(t) + sunEquationOfCenter(t);
  }

  /** Computes the Sun's apparent longitude (in degrees). */
  public static double sunApparentLongitude(double t) {
    double omega = 125.04 - 1934.136 * t;
    return sunTrueLongitude(t) - 0.00569 - 0.00478 * Math.sin(Math.toRadians(omega));
  }

  /** Computes the mean obliquity of the ecliptic (in degrees). */
  public static double meanObliquityOfEcliptic(double t) {
    double seconds = 21.448 - t * (46.8150 + t * (0.00059 - t * 0.001813));
    return 23.0 + (26.0 + (seconds / 60.0)) / 60.0;
  }

  /** Computes the corrected obliquity of the ecliptic (in degrees). */
  public static double obliquityCorrection(double t) {
    double omega = 125.04 - 1934.136 * t;
    return meanObliquityOfEcliptic(t) + 0.00256 * Math.cos(Math.toRadians(omega));
  }

  /** Computes the Sun's declination (in degrees). */
  public static double sunDeclination(double t) {
    double e = Math.toRadians(obliquityCorrection(t));
    double lambda = Math.toRadians(sunApparentLongitude(t));
    return Math.toDegrees(Math.asin(Math.sin(e) * Math.sin(lambda)));
  }

  /** Computes the equation of time (in minutes). */
  public static double equationOfTime(double t) {
    double epsilon = Math.toRadians(obliquityCorrection(t));
    double l0 = Math.toRadians(sunMeanLongitude(t));
    double e = earthOrbitEccentricity(t);
    double m = Math.toRadians(sunMeanAnomaly(t));

    double y = Math.tan(epsilon / 2.0);
    y = y * y;

    double eqTime =
        y * Math.sin(2 * l0)
            - 2 * e * Math.sin(m)
            + 4 * e * y * Math.sin(m) * Math.cos(2 * l0)
            - 0.5 * y * y * Math.sin(4 * l0)
            - 1.25 * e * e * Math.sin(2 * m);

    return Math.toDegrees(eqTime) * 4.0;
  }

  /**
   * Computes the hour angle for a given sun angle below the horizon.
   *
   * @param latitude observer latitude in degrees
   * @param declination sun's declination in degrees
   * @param angle the sun angle below the horizon (positive value)
   * @return hour angle in degrees, or NaN if the sun doesn't reach that angle
   */
  public static double hourAngle(double latitude, double declination, double angle) {
    double latRad = Math.toRadians(latitude);
    double decRad = Math.toRadians(declination);
    double angleRad = Math.toRadians(-angle);

    double cosHA =
        (Math.sin(angleRad) - Math.sin(latRad) * Math.sin(decRad))
            / (Math.cos(latRad) * Math.cos(decRad));

    if (cosHA < -1.0 || cosHA > 1.0) {
      return Double.NaN;
    }

    return Math.toDegrees(Math.acos(cosHA));
  }

  /**
   * Computes solar noon in fractional hours (UTC).
   *
   * @param jd Julian Date
   * @param longitude observer longitude in degrees
   * @return fractional hours from midnight UTC
   */
  public static double solarNoon(double jd, double longitude) {
    double t = julianCentury(jd);
    double eqTime = equationOfTime(t);
    return (720 - 4 * longitude - eqTime) / 60.0;
  }

  /**
   * Computes the time for a given sun angle.
   *
   * @param jd Julian Date
   * @param latitude observer latitude in degrees
   * @param longitude observer longitude in degrees
   * @param angle sun angle below horizon (positive for below)
   * @param afterNoon true for afternoon events (Asr, Maghrib, Isha), false for morning (Fajr,
   *     Sunrise)
   * @return fractional hours from midnight UTC, or NaN if not available
   */
  public static double timeForAngle(
      double jd, double latitude, double longitude, double angle, boolean afterNoon) {
    double t = julianCentury(jd);
    double eqTime = equationOfTime(t);
    double declination = sunDeclination(t);
    double ha = hourAngle(latitude, declination, angle);

    if (Double.isNaN(ha)) {
      return Double.NaN;
    }

    double noon = (720 - 4 * longitude - eqTime) / 60.0;
    double offset = ha * 4.0 / 60.0;

    return afterNoon ? noon + offset : noon - offset;
  }

  /**
   * Computes the Asr time using the shadow length ratio method.
   *
   * @param jd Julian Date
   * @param latitude observer latitude in degrees
   * @param longitude observer longitude in degrees
   * @param shadowRatio 1 for Shafi'i/Standard, 2 for Hanafi
   * @return fractional hours from midnight UTC
   */
  public static double asrTime(double jd, double latitude, double longitude, int shadowRatio) {
    double t = julianCentury(jd);
    double declination = sunDeclination(t);
    double eqTime = equationOfTime(t);

    double latRad = Math.toRadians(latitude);
    double decRad = Math.toRadians(declination);

    double asrAngle =
        Math.toDegrees(Math.atan(1.0 / (shadowRatio + Math.tan(Math.abs(latRad - decRad)))));

    double ha = hourAngle(latitude, declination, asrAngle);

    if (Double.isNaN(ha)) {
      return Double.NaN;
    }

    double noon = (720 - 4 * longitude - eqTime) / 60.0;
    return noon + ha * 4.0 / 60.0;
  }
}
