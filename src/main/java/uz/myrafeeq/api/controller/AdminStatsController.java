package uz.myrafeeq.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.myrafeeq.api.dto.response.AdminStatsResponse;
import uz.myrafeeq.api.service.admin.AdminStatsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/stats")
@Tag(name = "Admin - Stats", description = "System statistics")
@SecurityRequirement(name = "adminApiKey")
public class AdminStatsController {

  private final AdminStatsService statsService;

  @GetMapping
  @Operation(summary = "Get system statistics")
  public ResponseEntity<AdminStatsResponse> getStats() {
    return ResponseEntity.ok(statsService.getStats());
  }
}
