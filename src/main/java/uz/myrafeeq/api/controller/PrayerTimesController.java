package uz.myrafeeq.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.myrafeeq.api.dto.response.PrayerTimesResponse;
import uz.myrafeeq.api.enums.CalculationMethod;
import uz.myrafeeq.api.enums.Madhab;
import uz.myrafeeq.api.service.prayer.PrayerTimesService;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/prayer-times")
@Tag(name = "Prayer Times", description = "Prayer time calculations")
public class PrayerTimesController {

  private final PrayerTimesService prayerTimesService;

  @GetMapping
  @Operation(
      summary = "Get prayer times",
      description =
          """
          Returns prayer times for the authenticated user based on their \
          saved preferences and location.""")
  public ResponseEntity<List<PrayerTimesResponse>> getPrayerTimes(
      @AuthenticationPrincipal Long telegramId,
      @Parameter(description = "Date (defaults to today)", example = "2026-02-24")
          @RequestParam(required = false)
          LocalDate date,
      @Parameter(description = "Number of days (1-30, defaults to 1)", example = "1")
          @RequestParam(required = false, defaultValue = "1")
          @Min(1)
          @Max(30)
          int days) {

    return ResponseEntity.ok(prayerTimesService.calculatePrayerTimes(telegramId, date, days));
  }

  @GetMapping("/by-location")
  @Operation(
      summary = "Get prayer times by location",
      description =
          """
          Returns prayer times for a given location. Does not require \
          authentication. Useful for anonymous or preview usage.""")
  public ResponseEntity<PrayerTimesResponse> getPrayerTimesByLocation(
      @Parameter(description = "Latitude (-90 to 90)", example = "41.2995")
          @RequestParam
          @DecimalMin("-90")
          @DecimalMax("90")
          double lat,
      @Parameter(description = "Longitude (-180 to 180)", example = "69.2401")
          @RequestParam
          @DecimalMin("-180")
          @DecimalMax("180")
          double lon,
      @Parameter(description = "Date (defaults to today)", example = "2026-02-24")
          @RequestParam(required = false)
          LocalDate date,
      @Parameter(description = "Calculation method (defaults to MWL)", example = "MWL")
          @RequestParam(required = false)
          CalculationMethod method,
      @Parameter(description = "IANA timezone (defaults to UTC)", example = "Asia/Tashkent")
          @RequestParam(required = false)
          String timezone,
      @Parameter(description = "Madhab (defaults to SHAFI)", example = "HANAFI")
          @RequestParam(required = false)
          Madhab madhab) {

    return ResponseEntity.ok(
        prayerTimesService.calculatePrayerTimesByLocation(
            lat, lon, date, method, timezone, madhab));
  }
}
