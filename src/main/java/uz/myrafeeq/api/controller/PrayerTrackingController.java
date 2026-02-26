package uz.myrafeeq.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.myrafeeq.api.dto.request.TogglePrayerRequest;
import uz.myrafeeq.api.dto.response.ErrorResponse;
import uz.myrafeeq.api.dto.response.PrayerStatsResponse;
import uz.myrafeeq.api.dto.response.PrayerTrackingResponse;
import uz.myrafeeq.api.dto.response.TogglePrayerResponse;
import uz.myrafeeq.api.enums.StatsPeriod;
import uz.myrafeeq.api.service.prayer.PrayerTrackingService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/prayer-tracking")
@Tag(name = "Prayer Tracking", description = "Track daily prayer completion")
public class PrayerTrackingController {

  private final PrayerTrackingService trackingService;

  @GetMapping
  @Operation(
      summary = "Get prayer tracking",
      description = "Returns prayer tracking data for a date or date range.")
  @ApiResponse(responseCode = "200", description = "Tracking data retrieved successfully")
  public ResponseEntity<PrayerTrackingResponse> getTracking(
      @AuthenticationPrincipal Long telegramId,
      @Parameter(description = "Single date", example = "2026-02-24")
          @RequestParam(required = false)
          LocalDate date,
      @Parameter(description = "Range start date", example = "2026-02-17")
          @RequestParam(required = false)
          LocalDate from,
      @Parameter(description = "Range end date", example = "2026-02-24")
          @RequestParam(required = false)
          LocalDate to) {

    return ResponseEntity.ok(trackingService.getTracking(telegramId, date, from, to));
  }

  @PostMapping("/toggle")
  @Operation(
      summary = "Toggle prayer status",
      description =
          """
          Toggles whether a specific prayer was performed. Date must not be \
          in the future and not older than 7 days.""")
  @ApiResponse(responseCode = "200", description = "Prayer status toggled successfully")
  @ApiResponse(
      responseCode = "400",
      description = "Validation error",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<TogglePrayerResponse> togglePrayer(
      @AuthenticationPrincipal Long telegramId, @Valid @RequestBody TogglePrayerRequest request) {

    return ResponseEntity.ok(trackingService.togglePrayer(telegramId, request));
  }

  @GetMapping("/stats")
  @Operation(
      summary = "Get prayer statistics",
      description =
          """
          Returns prayer completion statistics for a given period \
          (WEEK, MONTH, YEAR).""")
  @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
  public ResponseEntity<PrayerStatsResponse> getStats(
      @AuthenticationPrincipal Long telegramId,
      @Parameter(description = "Period: WEEK, MONTH, YEAR", example = "WEEK")
          @RequestParam(defaultValue = "WEEK")
          StatsPeriod period) {

    return ResponseEntity.ok(trackingService.getStats(telegramId, period));
  }
}
