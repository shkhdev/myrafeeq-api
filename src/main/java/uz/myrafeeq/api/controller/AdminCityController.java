package uz.myrafeeq.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.myrafeeq.api.dto.request.BulkCreateCitiesRequest;
import uz.myrafeeq.api.dto.request.CreateCityRequest;
import uz.myrafeeq.api.dto.request.UpdateCityRequest;
import uz.myrafeeq.api.dto.response.AdminCityResponse;
import uz.myrafeeq.api.dto.response.BulkCreateCitiesResponse;
import uz.myrafeeq.api.service.admin.AdminCityService;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/cities")
@Tag(name = "Admin - Cities", description = "Admin management of city reference data")
public class AdminCityController {

  private final AdminCityService cityService;

  @GetMapping
  @Operation(summary = "List cities (paginated)")
  public ResponseEntity<Page<AdminCityResponse>> listCities(
      @Parameter(description = "Filter by country code") @RequestParam(required = false)
          String country,
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
      @Parameter(description = "Page size (1-100)")
          @RequestParam(defaultValue = "20")
          @Min(1) @Max(100) int size) {
    return ResponseEntity.ok(cityService.listCities(country, page, size));
  }

  @PostMapping
  @Operation(summary = "Create a city")
  public ResponseEntity<AdminCityResponse> createCity(
      @Valid @RequestBody CreateCityRequest request) {
    AdminCityResponse response = cityService.createCity(request);
    return ResponseEntity.created(URI.create("/api/v1/admin/cities/" + response.getId()))
        .body(response);
  }

  @PostMapping("/bulk")
  @Operation(summary = "Bulk create cities")
  public ResponseEntity<BulkCreateCitiesResponse> bulkCreateCities(
      @Valid @RequestBody BulkCreateCitiesRequest request) {
    return ResponseEntity.created(URI.create("/api/v1/admin/cities"))
        .body(cityService.bulkCreateCities(request));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a city by ID")
  public ResponseEntity<AdminCityResponse> getCity(@PathVariable String id) {
    return ResponseEntity.ok(cityService.getCity(id));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a city")
  public ResponseEntity<AdminCityResponse> updateCity(
      @PathVariable String id, @Valid @RequestBody UpdateCityRequest request) {
    return ResponseEntity.ok(cityService.updateCity(id, request));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a city")
  public ResponseEntity<Void> deleteCity(@PathVariable String id) {
    cityService.deleteCity(id);
    return ResponseEntity.noContent().build();
  }
}
