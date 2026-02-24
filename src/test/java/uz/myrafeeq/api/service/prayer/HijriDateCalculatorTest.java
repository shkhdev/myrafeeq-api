package uz.myrafeeq.api.service.prayer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class HijriDateCalculatorTest {

  private static final List<String> HIJRI_MONTH_NAMES =
      List.of(
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

  @Test
  void returnsNonNullNonEmpty() {
    String result = HijriDateCalculator.toHijriDate(LocalDate.of(2026, 2, 24));
    assertThat(result).isNotNull().isNotEmpty();
  }

  @Test
  void containsValidMonthName() {
    String result = HijriDateCalculator.toHijriDate(LocalDate.of(2026, 2, 24));
    assertThat(HIJRI_MONTH_NAMES).anyMatch(result::contains);
  }

  @Test
  void knownDate() {
    // Feb 24, 2026 is 7 Ramadan 1447 in the Umm Al-Qura calendar
    String result = HijriDateCalculator.toHijriDate(LocalDate.of(2026, 2, 24));
    assertThat(result).isEqualTo("7 Ramadan 1447");
  }

  @Test
  void hijriYearIsReasonable() {
    // For dates 2024-2030, verify the year increases monotonically
    // and falls within the correct Hijri range (1446-1452)
    int previousYear = 0;
    for (int year = 2024; year <= 2030; year++) {
      String result = HijriDateCalculator.toHijriDate(LocalDate.of(year, 6, 15));
      String[] parts = result.split(" ");
      String yearStr = parts[parts.length - 1];
      int hijriYear = Integer.parseInt(yearStr);
      assertThat(hijriYear).isBetween(1445, 1453);
      assertThat(hijriYear).isGreaterThanOrEqualTo(previousYear);
      previousYear = hijriYear;
    }
  }

  @Test
  void formattedCorrectly() {
    String result = HijriDateCalculator.toHijriDate(LocalDate.of(2026, 2, 24));
    assertThat(result).matches("\\d+ [A-Za-z' -]+ \\d+");
  }
}
