package io.github.brainzy.rankdrop.controller;

import io.github.brainzy.rankdrop.dto.LeaderboardCreateRequest;
import io.github.brainzy.rankdrop.dto.LeaderboardUpdateRequest;
import io.github.brainzy.rankdrop.entity.Leaderboard;
import io.github.brainzy.rankdrop.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/leaderboards")
@Tag(name = "Admin Leaderboard Management", description = "Privileged operations for managing leaderboard configurations")
public class AdminController {

    private final LeaderboardService leaderboardService;

    public AdminController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @PostMapping
    @Operation(summary = "Create a new leaderboard", description = "Initializes a leaderboard with a unique slug and sorting rules.")
    @ApiResponse(responseCode = "201", description = "Leaderboard created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "409", description = "Leaderboard with this slug already exists", content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<Leaderboard> create(@Valid @RequestBody LeaderboardCreateRequest request) {
        Leaderboard saved = leaderboardService.createNewLeaderboard(request);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/{slug}")
    @Operation(summary = "Update leaderboard settings", description = "Allows changing the display name of an existing leaderboard.")
    @ApiResponse(responseCode = "200", description = "Leaderboard updated successfully")
    @ApiResponse(responseCode = "404", description = "Leaderboard not found", content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<Leaderboard> update(@PathVariable String slug, @Valid @RequestBody LeaderboardUpdateRequest request) {
        return ResponseEntity.ok(
                leaderboardService.updateExistingLeaderboard(slug, request.displayName())
        );
    }

    @DeleteMapping("/{slug}")
    @Operation(summary = "Delete a leaderboard", description = "Permanently removes a leaderboard and all its associated scores.")
    @ApiResponse(responseCode = "204", description = "Leaderboard deleted successfully")
    @ApiResponse(responseCode = "404", description = "Leaderboard not found", content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<Void> delete(@PathVariable String slug) {
        leaderboardService.deleteLeaderboardBySlug(slug);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List all leaderboards", description = "Returns a complete list of all active leaderboard configurations.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of leaderboards")
    public List<Leaderboard> list() {
        return leaderboardService.getAllLeaderboards();
    }
}