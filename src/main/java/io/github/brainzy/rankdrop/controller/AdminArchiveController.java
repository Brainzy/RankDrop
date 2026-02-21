package io.github.brainzy.rankdrop.controller;

import io.github.brainzy.rankdrop.dto.ScoreArchiveSummary;
import io.github.brainzy.rankdrop.entity.ScoreArchive;
import io.github.brainzy.rankdrop.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin — Archive", description = "Archive history")
public class AdminArchiveController {

    private final LeaderboardService leaderboardService;

    public AdminArchiveController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
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
