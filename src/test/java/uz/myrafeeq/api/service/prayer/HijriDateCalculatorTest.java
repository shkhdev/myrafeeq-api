package uz.myrafeeq.api.service.prayer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HijriDateCalculatorTest {

  @Test
  void should_returnFormattedHijriDate_when_validGregorianDate() {
    String result = HijriDateCalculator.toHijriDate(LocalDate.of(2025, 3, 10), 0);

    assertThat(result).isNotBlank();
    assertThat(result).matches("\\d+ .+ \\d+");
  }

  @Test
  void should_applyPositiveCorrection_when_correctionDaysPositive() {
    LocalDate date = LocalDate.of(2025, 3, 10);
    String withoutCorrection = HijriDateCalculator.toHijriDate(date, 0);
    String withCorrection = HijriDateCalculator.toHijriDate(date, 2);

    assertThat(withCorrection).isNotEqualTo(withoutCorrection);
  }

  @Test
  void should_applyNegativeCorrection_when_correctionDaysNegative() {
    LocalDate date = LocalDate.of(2025, 3, 10);
    String withoutCorrection = HijriDateCalculator.toHijriDate(date, 0);
    String withNegativeCorrection = HijriDateCalculator.toHijriDate(date, -2);

    assertThat(withNegativeCorrection).isNotEqualTo(withoutCorrection);
  }

  @ParameterizedTest
  @ValueSource(ints = {-2, -1, 0, 1, 2})
  void should_handleAllCorrectionValues_when_withinRange(int correction) {
    String result = HijriDateCalculator.toHijriDate(LocalDate.of(2025, 6, 15), correction);

    assertThat(result).isNotBlank();
    assertThat(result).matches("\\d+ .+ \\d+");
  }

  @Test
  void should_returnConsistentResult_when_sameDateCalledTwice() {
    LocalDate date = LocalDate.of(2025, 1, 1);
    String result1 = HijriDateCalculator.toHijriDate(date, 0);
    String result2 = HijriDateCalculator.toHijriDate(date, 0);

    assertThat(result1).isEqualTo(result2);
  }

  @Test
  void should_containValidMonthName_when_knownRamadanDate() {
    // March 2025 is around Ramadan 1446
    String result = HijriDateCalculator.toHijriDate(LocalDate.of(2025, 3, 15), 0);

    assertThat(result)
        .containsAnyOf(
            "Muharram",
            "Safar",
            "Rabi' al-Awwal",
            "Rabi' al-Thani",
            "Jumada al-Ula",
            "Jumada al-Thani",
            "Rajab",
            "Sha'ban",
            "Ramadan",
            "Shawwal",
            "Dhu al-Qi'dah",
            "Dhu al-Hijjah");
  }

  @Test
  void should_handleYearBoundary_when_newYearDate() {
    String result = HijriDateCalculator.toHijriDate(LocalDate.of(2025, 1, 1), 0);

    assertThat(result).isNotBlank();
    assertThat(result).matches("\\d+ .+ \\d+");
  }

  @Test
  void should_handleLeapYearDate_when_feb29() {
    String result = HijriDateCalculator.toHijriDate(LocalDate.of(2024, 2, 29), 0);

    assertThat(result).isNotBlank();
  }
}
