package io.github.brainzy.rankdrop.controller;

import io.github.brainzy.rankdrop.dto.LeaderboardCreateRequest;
import io.github.brainzy.rankdrop.dto.LeaderboardResetRequest;
import io.github.brainzy.rankdrop.dto.LeaderboardUpdateRequest;
import io.github.brainzy.rankdrop.dto.ScoreArchiveSummary;
import io.github.brainzy.rankdrop.entity.Leaderboard;
import io.github.brainzy.rankdrop.entity.Player;
import io.github.brainzy.rankdrop.entity.ScoreArchive;
import io.github.brainzy.rankdrop.service.LeaderboardService;
import io.github.brainzy.rankdrop.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin Leaderboard Management", description = "Privileged operations for managing leaderboard configurations")
public class AdminController {

    private final LeaderboardService leaderboardService;
    private final PlayerService playerService;

    public AdminController(LeaderboardService leaderboardService, PlayerService playerService) {

        this.leaderboardService = leaderboardService;
        this.playerService = playerService;
    }

    @PostMapping("/players/{playerAlias}/ban")
    @Operation(summary = "Ban a player globally", description = "Bans a player from submitting scores to all leaderboards.")
    @ApiResponse(responseCode = "200", description = "Player banned successfully")
    public ResponseEntity<Player> banPlayer(
            @PathVariable String playerAlias,
            @RequestParam(required = false) String reason
    ) {
        Player bannedPlayer = playerService.banPlayer(playerAlias, reason);
        return ResponseEntity.ok(bannedPlayer);
    }

    @PostMapping("/leaderboards")
    @Operation(summary = "Create a new leaderboard", description = "Initializes a leaderboard with a unique slug and sorting rules.")
    @ApiResponse(responseCode = "201", description = "Leaderboard created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "409", description = "Leaderboard with this slug already exists", content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<Leaderboard> create(@Valid @RequestBody LeaderboardCreateRequest request) {
        Leaderboard saved = leaderboardService.createNewLeaderboard(request);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/leaderboards/{slug}")
    @Operation(summary = "Update leaderboard settings", description = "Allows changing the display name of an existing leaderboard.")
    @ApiResponse(responseCode = "200", description = "Leaderboard updated successfully")
    @ApiResponse(responseCode = "404", description = "Leaderboard not found", content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<Leaderboard> update(@PathVariable String slug, @Valid @RequestBody LeaderboardUpdateRequest request) {
        return ResponseEntity.ok(
                leaderboardService.updateExistingLeaderboard(slug, request.displayName())
        );
    }

    @DeleteMapping("/leaderboards/{slug}")
    @Operation(summary = "Delete a leaderboard", description = "Permanently removes a leaderboard and all its associated scores.")
    @ApiResponse(responseCode = "204", description = "Leaderboard deleted successfully")
    @ApiResponse(responseCode = "404", description = "Leaderboard not found", content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<Void> delete(@PathVariable String slug) {
        leaderboardService.deleteLeaderboardBySlug(slug);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/leaderboards")
    @Operation(summary = "List all leaderboards", description = "Returns a complete list of all active leaderboard configurations.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of leaderboards")
    public List<Leaderboard> list() {
        return leaderboardService.getAllLeaderboards();
    }

    @PostMapping("/leaderboards/{slug}/reset")
    @Operation(summary = "Reset a leaderboard", description = "Clears all scores from a leaderboard, optionally archiving them first.")
    @ApiResponse(responseCode = "204", description = "Leaderboard reset successfully")
    @ApiResponse(responseCode = "404", description = "Leaderboard not found", content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "400", description = "Invalid input (e.g. missing resetLabel when archiving)", content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<Void> reset(@PathVariable String slug, @Valid @RequestBody LeaderboardResetRequest request) {
        leaderboardService.resetLeaderboard(slug, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/archive")
    @Operation(
            summary = "Get global archive history",
            description = "Lists all archive snapshots across all leaderboards."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved global archive history")
    public List<ScoreArchiveSummary> getAllArchives() {
        return leaderboardService.getAllArchives();
    }

    @GetMapping("/archive/{slug}/{resetLabel}")
    @Operation(
            summary = "Get specific archived snapshot",
            description = "Returns actual score entries for a specific archive snapshot identified by its reset label."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved archived scores")
    @ApiResponse(responseCode = "404", description = "Leaderboard not found", content = @Content(schema = @Schema(hidden = true)))
    public List<ScoreArchive> getArchivedScoresByLabel(
            @Parameter(description = "The unique slug of the leaderboard", example = "global-high-scores")
            @PathVariable String slug,

            @Parameter(description = "The label of the archive snapshot", example = "Season 1")
            @PathVariable String resetLabel,

            @Parameter(description = "Number of scores to return", example = "50")
            @RequestParam(defaultValue = "50") int limit
    ) {
        return leaderboardService.getArchivedScoresByLabel(slug, resetLabel, limit);
    }
}