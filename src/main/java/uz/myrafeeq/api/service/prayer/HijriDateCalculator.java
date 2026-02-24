package uz.myrafeeq.api.service.prayer;

import java.time.LocalDate;

/**
 * Computes approximate Hijri dates using the Tabular Islamic Calendar. This is a simplified
 * approximation and should be adjusted with hijriCorrection from user preferences for accuracy.
 */
public final class HijriDateCalculator {

  private static final long HIJRI_EPOCH_OFFSET = 227015;
  private static final int DAYS_PER_CYCLE = 10631;
  private static final int YEARS_PER_CYCLE = 30;
  private static final int[] MONTH_DAYS = {30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29};
  private static final int[] LEAP_YEARS = {2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29};
  private static final String[] MONTH_NAMES = {
    "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
    "Jumada al-Ula", "Jumada al-Thani", "Rajab", "Sha'ban",
    "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
  };

  private HijriDateCalculator() {}

  public static String toHijriDate(LocalDate gregorianDate) {
    long daysSinceHijriEpoch = gregorianDate.toEpochDay() + HIJRI_EPOCH_OFFSET;

    long cycle = daysSinceHijriEpoch / DAYS_PER_CYCLE;
    long remaining = daysSinceHijriEpoch % DAYS_PER_CYCLE;

    int yearInCycle = 0;
    for (int y = 0; y < YEARS_PER_CYCLE && remaining > 0; y++) {
      int daysInYear = isLeapYear(y + 1) ? 355 : 354;
      if (remaining < daysInYear) {
        yearInCycle = y;
        break;
      }
      remaining -= daysInYear;
      yearInCycle = y + 1;
    }

    long hijriYear = cycle * YEARS_PER_CYCLE + yearInCycle + 1;
    boolean isLeap = isLeapYear(yearInCycle + 1);

    int hijriMonth = 0;
    for (int m = 0; m < 12; m++) {
      int daysInMonth = MONTH_DAYS[m];
      if (m == 11 && isLeap) {
        daysInMonth = 30;
      }
      if (remaining < daysInMonth) {
        hijriMonth = m + 1;
        break;
      }
      remaining -= daysInMonth;
    }
    if (hijriMonth == 0) hijriMonth = 12;

    long hijriDay = remaining + 1;
    String monthName = MONTH_NAMES[Math.max(0, Math.min(11, hijriMonth - 1))];
    return hijriDay + " " + monthName + " " + hijriYear;
  }

  private static boolean isLeapYear(int yearInCycle) {
    for (int ly : LEAP_YEARS) {
      if (yearInCycle == ly) return true;
    }
    return false;
  }
}
