package uz.myrafeeq.api;

import java.time.Instant;
import java.util.UUID;
import uz.myrafeeq.api.dto.response.CityResponse;
import uz.myrafeeq.api.dto.response.UserPreferencesResponse;
import uz.myrafeeq.api.dto.response.UserResponse;
import uz.myrafeeq.api.entity.CityEntity;
import uz.myrafeeq.api.entity.CountryEntity;
import uz.myrafeeq.api.entity.PrayerTrackingEntity;
import uz.myrafeeq.api.entity.UserEntity;
import uz.myrafeeq.api.entity.UserPreferencesEntity;
import uz.myrafeeq.api.enums.CalculationMethod;
import uz.myrafeeq.api.enums.HighLatitudeRule;
import uz.myrafeeq.api.enums.Madhab;
import uz.myrafeeq.api.enums.PrayerName;

public final class TestDataFactory {

  public static final Long TELEGRAM_ID = 123456789L;
  public static final double TASHKENT_LAT = 41.2995;
  public static final double TASHKENT_LON = 69.2401;

  private TestDataFactory() {}

  public static CountryEntity.CountryEntityBuilder aCountry() {
    return CountryEntity.builder()
        .code("UZ")
        .name("Uzbekistan")
        .defaultMethod(CalculationMethod.MBOUZ)
        .defaultMadhab(Madhab.HANAFI);
  }

  public static CityEntity.CityEntityBuilder aCity() {
    return CityEntity.builder()
        .id("tashkent")
        .name("Tashkent")
        .country(aCountry().build())
        .latitude(TASHKENT_LAT)
        .longitude(TASHKENT_LON)
        .timezone("Asia/Tashkent");
  }

  public static UserEntity.UserEntityBuilder aUser() {
    return UserEntity.builder()
        .telegramId(TELEGRAM_ID)
        .firstName("Doston")
        .username("doston")
        .onboardingCompleted(false);
  }

  public static UserPreferencesEntity.UserPreferencesEntityBuilder aPreferences() {
    return UserPreferencesEntity.builder()
        .telegramId(TELEGRAM_ID)
        .cityId("tashkent")
        .latitude(TASHKENT_LAT)
        .longitude(TASHKENT_LON)
        .languageCode("uz")
        .calculationMethod(CalculationMethod.MBOUZ)
        .madhab(Madhab.HANAFI)
        .highLatitudeRule(HighLatitudeRule.MIDDLE_OF_NIGHT)
        .hijriCorrection(0);
  }

  public static PrayerTrackingEntity.PrayerTrackingEntityBuilder aTracking() {
    return PrayerTrackingEntity.builder()
        .id(UUID.randomUUID())
        .telegramId(TELEGRAM_ID)
        .prayerName(PrayerName.FAJR)
        .prayed(true)
        .toggledAt(Instant.now());
  }

  public static CityResponse.CityResponseBuilder aCityResponse() {
    return CityResponse.builder()
        .id("tashkent")
        .name("Tashkent")
        .country("UZ")
        .latitude(TASHKENT_LAT)
        .longitude(TASHKENT_LON)
        .timezone("Asia/Tashkent")
        .defaultMethod("MBOUZ")
        .defaultMadhab("HANAFI");
  }

  public static UserResponse.UserResponseBuilder aUserResponse() {
    return UserResponse.builder()
        .telegramId(TELEGRAM_ID)
        .firstName("Doston")
        .onboardingCompleted(false);
  }

  public static UserPreferencesResponse.UserPreferencesResponseBuilder aPreferencesResponse() {
    return UserPreferencesResponse.builder()
        .languageCode("uz")
        .calculationMethod("MBOUZ")
        .madhab("HANAFI")
        .hijriCorrection(0);
  }
}
