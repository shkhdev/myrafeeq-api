package uz.myrafeeq.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.myrafeeq.api.dto.response.CitySearchResponse;
import uz.myrafeeq.api.dto.response.ErrorResponse;
import uz.myrafeeq.api.dto.response.NearestCityResponse;
import uz.myrafeeq.api.service.city.CityService;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cities")
@Tag(name = "Cities", description = "City search and lookup")
public class CityController {

  private final CityService cityService;

  @GetMapping
  @Operation(summary = "Search cities", description = "Searches cities by name.")
  @ApiResponse(responseCode = "200", description = "Search results returned")
  @ApiResponse(
      responseCode = "400",
      description = "Invalid parameters",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<CitySearchResponse> searchCities(
      @Parameter(description = "Search query", example = "Tashkent") @RequestParam String q,
      @Parameter(description = "Maximum results (1-50)", example = "10")
          @RequestParam(required = false, defaultValue = "10")
          @Min(1) @Max(50) int limit) {

    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS).cachePublic())
        .body(cityService.searchCities(q, limit));
  }

  @GetMapping("/nearest")
  @Operation(
      summary = "Find nearest city",
      description = "Finds the nearest city to the given coordinates using Haversine distance.")
  @ApiResponse(responseCode = "200", description = "Nearest city found")
  @ApiResponse(
      responseCode = "404",
      description = "No cities found near coordinates",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Invalid coordinates",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<NearestCityResponse> findNearestCity(
      @Parameter(description = "Latitude (-90 to 90)", example = "41.2995")
          @RequestParam
          @DecimalMin("-90") @DecimalMax("90") double lat,
      @Parameter(description = "Longitude (-180 to 180)", example = "69.2401")
          @RequestParam
          @DecimalMin("-180") @DecimalMax("180") double lon) {

    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS).cachePublic())
        .body(cityService.findNearestCity(lat, lon));
  }
}
