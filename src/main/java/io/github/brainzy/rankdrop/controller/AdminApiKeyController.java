package io.github.brainzy.rankdrop.controller;

import io.github.brainzy.rankdrop.dto.RotateKeyRequest;
import io.github.brainzy.rankdrop.service.SystemSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin — API Keys", description = "API key rotation")
public class AdminApiKeyController {

    private final SystemSettingService systemSettingService;

    public AdminApiKeyController(SystemSettingService systemSettingService) {
        this.systemSettingService = systemSettingService;
    }

    @PostMapping("/rotate-game-key")
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
            systemSettingService.setGameKey(newGameKey);
            return ResponseEntity.ok(Map.of("message", "Game key rotated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to rotate game key",
                            "message", e.getMessage()
                    ));
        }
    }
}
