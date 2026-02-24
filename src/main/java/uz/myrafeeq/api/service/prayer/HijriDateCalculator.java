package uz.myrafeeq.api.service.prayer;

import java.time.LocalDate;
import java.time.chrono.HijrahDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

/** Converts Gregorian dates to Hijri dates using the JDK's Umm Al-Qura calendar. */
public final class HijriDateCalculator {

  private static final String[] MONTH_NAMES = {
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
    "Dhu al-Hijjah"
  };

  private HijriDateCalculator() {}

  public static String toHijriDate(LocalDate gregorianDate, int correctionDays) {
    HijrahDate hijrahDate = HijrahDate.from(gregorianDate).plus(correctionDays, ChronoUnit.DAYS);
    int day = hijrahDate.get(ChronoField.DAY_OF_MONTH);
    int month = hijrahDate.get(ChronoField.MONTH_OF_YEAR);
    int year = hijrahDate.get(ChronoField.YEAR_OF_ERA);
    String monthName = MONTH_NAMES[month - 1];
    return day + " " + monthName + " " + year;
  }
}
