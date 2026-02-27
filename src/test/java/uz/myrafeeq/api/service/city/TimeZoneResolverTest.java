package uz.myrafeeq.api.service.city;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TimeZoneResolverTest {

  private static TimeZoneResolver resolver;

  @BeforeAll
  static void setUp() {
    resolver = new TimeZoneResolver();
    resolver.init();
  }

  @Test
  void should_resolveTashkent() {
    String tz = resolver.resolve(41.2995, 69.2401);
    assertThat(tz).isEqualTo("Asia/Tashkent");
  }

  @Test
  void should_resolveReykjavik() {
    String tz = resolver.resolve(64.1466, -21.9426);
    assertThat(tz).isEqualTo("Atlantic/Reykjavik");
  }

  @Test
  void should_resolveMecca() {
    String tz = resolver.resolve(21.4225, 39.8262);
    assertThat(tz).isEqualTo("Asia/Riyadh");
  }

  @Test
  void should_resolveIstanbul() {
    String tz = resolver.resolve(41.0082, 28.9784);
    assertThat(tz).isEqualTo("Europe/Istanbul");
  }

  @Test
  void should_resolveLondon() {
    String tz = resolver.resolve(51.5074, -0.1278);
    assertThat(tz).isEqualTo("Europe/London");
  }
}
