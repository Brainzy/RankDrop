package io.github.brainzy.rankdrop.controller;

import io.github.brainzy.rankdrop.dto.RotateKeyRequest;
import io.github.brainzy.rankdrop.dto.WebhookConfigRequest;
import io.github.brainzy.rankdrop.service.SystemSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin — Settings", description = "Config for game key and webhook")
public class AdminSettingsController {

    private final SystemSettingService systemSettingService;

    public AdminSettingsController(SystemSettingService systemSettingService) {
        this.systemSettingService = systemSettingService;
    }

    @PostMapping("/settings/game-key")
    @Operation(summary = "Rotate game key", description = "Updates the game key in database with provided value.")
    @ApiResponse(responseCode = "200", description = "Game key rotated successfully", content = @Content(schema = @Schema(example = "{\"message\": \"Game key rotated successfully\"}")))
    @ApiResponse(responseCode = "400", description = "Invalid key", content = @Content(schema = @Schema(example = "{\"error\": \"Invalid key\", \"message\": \"Game key must be at least 16 characters\"}")))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(example = "{\"error\": \"Failed to rotate game key\", \"message\": \"Database connection failed\"}")))
    public ResponseEntity<Map<String, String>> rotateGameKey(@RequestBody RotateKeyRequest request) {
        String newGameKey = request.newGameKey();

        if (newGameKey == null || newGameKey.length() < 16) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid key",
                    "message", "Game key must be at least 16 characters"
            ));
        }

        try {
            systemSettingService.setSetting("GAME_SECRET", newGameKey);
            return ResponseEntity.ok(Map.of("message", "Game key rotated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to rotate game key",
                            "message", e.getMessage()
                    ));
        }
    }

    @GetMapping("/settings")
    @Operation(summary = "Get all settings", description = "Retrieves all system settings from database.")
    @ApiResponse(responseCode = "200", description = "Settings retrieved successfully", content = @Content(schema = @Schema(example = "{\"WEBHOOK_URL\": \"https://example.com/webhook\", \"WEBHOOK_TOP_N\": \"10\", \"WEBHOOK_COOLDOWN_MS\": \"60000\", \"GAME_SECRET\": \"myGame_Secret_2026\", \"WEBHOOK_LAST_FIRED\": \"\"}")))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(example = "{\"error\": \"Failed to retrieve settings\", \"message\": \"Database connection failed\"}")))
    public ResponseEntity<Map<String, String>> getAllSettings() {
        try {
            List<String> settingKeys = List.of(
                    "GAME_SECRET",
                    "WEBHOOK_URL",
                    "WEBHOOK_TOP_N",
                    "WEBHOOK_COOLDOWN_MS",
                    "WEBHOOK_LAST_FIRED"
            );

            Map<String, String> settings = settingKeys.stream()
                    .collect(java.util.stream.Collectors.toMap(
                            key -> key,
                            key -> {
                                return systemSettingService.getSetting(key, "");
                            }
                    ));

            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to retrieve settings",
                            "message", e.getMessage()
                    ));
        }
    }

    @PostMapping("/settings/webhook")
    @Operation(summary = "Configure webhook settings", description = "Updates webhook configuration settings.")
    @ApiResponse(responseCode = "200", description = "Webhook configured successfully", content = @Content(schema = @Schema(example = "{\"message\": \"Webhook configured successfully\"}")))
    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(example = "{\"error\": \"Invalid request\", \"message\": \"Invalid webhook URL\"}")))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(example = "{\"error\": \"Failed to configure webhook\", \"message\": \"Database connection failed\"}")))
    public ResponseEntity<Map<String, String>> configureWebhook(@RequestBody WebhookConfigRequest request) {
        try {
            if (request.webhookUrl() != null && !request.webhookUrl().isBlank()) {
                systemSettingService.setSetting("WEBHOOK_URL", request.webhookUrl());
            }

            if (request.topN() != null && request.topN() > 0) {
                systemSettingService.setSetting("WEBHOOK_TOP_N", request.topN().toString());
            }

            if (request.cooldownMs() != null && request.cooldownMs() > 0) {
                systemSettingService.setSetting("WEBHOOK_COOLDOWN_MS", request.cooldownMs().toString());
            }

            return ResponseEntity.ok(Map.of("message", "Webhook configured successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to configure webhook",
                            "message", e.getMessage()
                    ));
        }
    }
}
