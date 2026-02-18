package io.github.brainzy.rankdrop.controller;

import io.github.brainzy.rankdrop.dto.ScoreEntryResponse;
import io.github.brainzy.rankdrop.dto.ScoreSubmissionRequest;
import io.github.brainzy.rankdrop.dto.ScoreSubmitResponse;
import io.github.brainzy.rankdrop.service.ScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaderboards")
@Tag(name = "Client API", description = "Public client API")
@Validated
@RequiredArgsConstructor
public class LeaderboardController {

    private final ScoreService scoreService;

    @GetMapping("/{slug}/top")
    @Operation(
            summary = "Get top scores",
            description = "Fetch the leaderboard rankings sorted according to the leaderboard configuration."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved top scores")
    @ApiResponse(responseCode = "404", description = "Leaderboard not found", content = @Content(schema = @Schema(hidden = true)))
    public List<ScoreEntryResponse> getTopScores(
            @Parameter(description = "The unique slug of the leaderboard", example = "global-high-scores")
            @PathVariable String slug,

            @Parameter(
                    description = "Number of top scores to return",
                    example = "10",
                    schema = @Schema(defaultValue = "10")
            )
            @RequestParam(defaultValue = "10") int limit
    ) {
        return scoreService.getTopScores(slug, limit);
    }

    @PostMapping("/{slug}/scores")
    @Operation(
            summary = "Submit a score",
            description = "Submit a new score for a player. The score will be validated against the leaderboard's min/max constraints."
    )
    @ApiResponse(responseCode = "200", description = "Score submitted successfully")
    @ApiResponse(responseCode = "404", description = "Leaderboard not found", content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(hidden = true)))
    public ScoreSubmitResponse submitScore(
            @Parameter(description = "The unique slug of the leaderboard", example = "global-high-scores")
            @PathVariable String slug,
            @Valid @RequestBody ScoreSubmissionRequest request) {
        return scoreService.submitScore(slug, request.playerAlias(), request.scoreValue());
    }

    @GetMapping("/{slug}/players/{playerAlias}")
    @Operation(
            summary = "Get player rank and surrounding scores",
            description = "Fetch a specific player's score and rank, optionally including surrounding players."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved player score")
    @ApiResponse(responseCode = "404", description = "Leaderboard or player not found", content = @Content(schema = @Schema(hidden = true)))
    public List<ScoreEntryResponse> getPlayerScore(
            @Parameter(description = "The unique slug of the leaderboard", example = "global-high-scores")
            @PathVariable String slug,

            @Parameter(description = "The player's alias/username", example = "PlayerOne")
            @PathVariable String playerAlias,

            @Parameter(
                    description = "Number of ranks above and below to fetch (e.g., 5 means ±5 ranks)",
                    example = "5",
                    schema = @Schema(defaultValue = "0")
            )
            @RequestParam(defaultValue = "0") int surrounding
    ) {
        return scoreService.getPlayerScoreWithSurrounding(slug, playerAlias, surrounding);
    }
}