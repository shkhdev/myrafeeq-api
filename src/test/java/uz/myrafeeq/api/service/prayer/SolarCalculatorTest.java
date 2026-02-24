package uz.myrafeeq.api.service.prayer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class SolarCalculatorTest {

  @Test
  void julianDateKnownEpoch() {
    // January 1, 2000 at 0h UT corresponds to JD 2451544.5
    double jd = SolarCalculator.julianDate(2000, 1, 1);
    assertThat(jd).isEqualTo(2451544.5);
  }

  @Test
  void julianDateLocalDate() {
    LocalDate date = LocalDate.of(2000, 1, 1);
    double jd = SolarCalculator.julianDate(date);
    assertThat(jd).isEqualTo(2451544.5);
  }

  @Test
  void julianCenturyJ2000() {
    // J2000.0 epoch is JD 2451545.0, which should yield Julian Century = 0.0
    double t = SolarCalculator.julianCentury(2451545.0);
    assertThat(t).isEqualTo(0.0);
  }

  @Test
  void sunDeclinationSummerSolstice() {
    // Around June 21, 2024 the sun declination should be close to +23.4 degrees
    double jd = SolarCalculator.julianDate(2024, 6, 21);
    double t = SolarCalculator.julianCentury(jd);
    double declination = SolarCalculator.sunDeclination(t);
    assertThat(declination).isBetween(22.0, 24.0);
  }

  @Test
  void sunDeclinationWinterSolstice() {
    // Around December 21, 2024 the sun declination should be close to -23.4 degrees
    double jd = SolarCalculator.julianDate(2024, 12, 21);
    double t = SolarCalculator.julianCentury(jd);
    double declination = SolarCalculator.sunDeclination(t);
    assertThat(declination).isBetween(-24.0, -22.0);
  }

  @Test
  void equationOfTimeReasonableRange() {
    // The equation of time should always be within approximately -17 to +17 minutes
    // Test across several dates throughout the year
    int[] months = {1, 3, 5, 7, 9, 11};
    for (int month : months) {
      double jd = SolarCalculator.julianDate(2024, month, 15);
      double t = SolarCalculator.julianCentury(jd);
      double eot = SolarCalculator.equationOfTime(t);
      assertThat(eot).isBetween(-17.0, 17.0);
    }
  }

  @Test
  void hourAngleReturnsNaNForImpossibleAngles() {
    // At latitude 60 degrees north with a very steep angle (e.g., 70 degrees below horizon),
    // the sun cannot reach that angle, so hourAngle should return NaN
    double jd = SolarCalculator.julianDate(2024, 6, 21);
    double t = SolarCalculator.julianCentury(jd);
    double declination = SolarCalculator.sunDeclination(t);
    double ha = SolarCalculator.hourAngle(60.0, declination, 70.0);
    assertThat(ha).isNaN();
  }

  @Test
  void solarNoonAtGreenwichIsNearNoon() {
    // At longitude 0 (Greenwich), solar noon should be around 12.0 UTC hours,
    // offset only by the equation of time (within roughly +/- 0.3 hours)
    double jd = SolarCalculator.julianDate(2024, 3, 20);
    double noon = SolarCalculator.solarNoon(jd, 0.0);
    assertThat(noon).isBetween(11.7, 12.3);
  }

  @Test
  void timeForAngleSunrise() {
    // Sunrise at Tashkent (lat 41.3, lon 69.3) on Feb 24, 2026
    // Sunrise angle is 0.833 degrees (standard atmospheric refraction).
    // Sunrise in Tashkent is around 07:05 local (UTC+5), so roughly 02:05 UTC
    double jd = SolarCalculator.julianDate(2026, 2, 24);
    double sunrise = SolarCalculator.timeForAngle(jd, 41.3, 69.3, 0.833, false);
    // Should be a reasonable UTC hour for sunrise (between 1.0 and 3.0 UTC)
    assertThat(sunrise).isBetween(1.0, 3.0);
  }

  @Test
  void asrTimeReasonable() {
    // Asr at Tashkent (lat 41.3, lon 69.3) on Feb 24, 2026
    // Using Shafi'i method (shadowRatio=1)
    double jd = SolarCalculator.julianDate(2026, 2, 24);
    double asr = SolarCalculator.asrTime(jd, 41.3, 69.3, 1);
    // Asr must be after solar noon
    double noon = SolarCalculator.solarNoon(jd, 69.3);
    assertThat(asr).isGreaterThan(noon);
    // Asr should be a valid positive UTC hour value within the day
    assertThat(asr).isBetween(0.0, 24.0);
  }
}
