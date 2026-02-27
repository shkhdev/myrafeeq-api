package uz.myrafeeq.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.myrafeeq.api.dto.response.DashboardResponse;
import uz.myrafeeq.api.dto.response.ErrorResponse;
import uz.myrafeeq.api.service.dashboard.DashboardService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Aggregated data for the main screen")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping
  @Operation(
      summary = "Get dashboard data",
      description =
          """
          Returns aggregated data for the main screen: today's prayer times, \
          today's tracking status, and weekly statistics. Reduces multiple \
          API calls to a single request.""")
  @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully")
  @ApiResponse(
      responseCode = "404",
      description = "Preferences not found (onboarding not completed)",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<DashboardResponse> getDashboard(
      @Parameter(hidden = true) @AuthenticationPrincipal Long telegramId) {

    return ResponseEntity.ok(dashboardService.getDashboard(telegramId));
  }
}
