package uz.myrafeeq.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.myrafeeq.api.dto.request.CreateCountryRequest;
import uz.myrafeeq.api.dto.request.UpdateCountryRequest;
import uz.myrafeeq.api.dto.response.CountryResponse;
import uz.myrafeeq.api.dto.response.ErrorResponse;
import uz.myrafeeq.api.service.admin.AdminCountryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/countries")
@Tag(name = "Admin - Countries", description = "Admin management of country reference data")
@SecurityRequirement(name = "adminApiKey")
public class AdminCountryController {

  private final AdminCountryService countryService;

  @GetMapping
  @Operation(summary = "List all countries")
  public ResponseEntity<List<CountryResponse>> listCountries() {
    return ResponseEntity.ok(countryService.listCountries());
  }

  @PostMapping
  @Operation(summary = "Create a country")
  @ApiResponse(responseCode = "201", description = "Country created")
  @ApiResponse(
      responseCode = "400",
      description = "Validation error",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "409",
      description = "Country already exists",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<CountryResponse> createCountry(
      @Valid @RequestBody CreateCountryRequest request) {
    CountryResponse response = countryService.createCountry(request);
    return ResponseEntity.created(URI.create("/api/v1/admin/countries/" + response.getCode()))
        .body(response);
  }

  @GetMapping("/{code}")
  @Operation(summary = "Get a country by code")
  @ApiResponse(responseCode = "200", description = "Country found")
  @ApiResponse(
      responseCode = "404",
      description = "Country not found",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<CountryResponse> getCountry(@PathVariable String code) {
    return ResponseEntity.ok(countryService.getCountry(code));
  }

  @PutMapping("/{code}")
  @Operation(summary = "Update a country")
  @ApiResponse(responseCode = "200", description = "Country updated")
  @ApiResponse(
      responseCode = "404",
      description = "Country not found",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Validation error",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<CountryResponse> updateCountry(
      @PathVariable String code, @Valid @RequestBody UpdateCountryRequest request) {
    return ResponseEntity.ok(countryService.updateCountry(code, request));
  }

  @DeleteMapping("/{code}")
  @Operation(summary = "Delete a country")
  @ApiResponse(responseCode = "204", description = "Country deleted")
  @ApiResponse(
      responseCode = "404",
      description = "Country not found",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<Void> deleteCountry(@PathVariable String code) {
    countryService.deleteCountry(code);
    return ResponseEntity.noContent().build();
  }
}
