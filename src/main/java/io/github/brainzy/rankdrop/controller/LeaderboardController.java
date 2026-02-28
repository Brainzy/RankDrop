package io.github.brainzy.rankdrop.controller;

import io.github.brainzy.rankdrop.dto.*;
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
@Tag(name = "Client — API", description = "Score submission and leaderboard reads")
@Validated
@RequiredArgsConstructor
public class LeaderboardController {

    private final ScoreService scoreService;

    @GetMapping("/{slug}/top")
    @Operation(
            summary = "Get top scores",
            description = "Fetch the leaderboard rankings sorted according to the leaderboard configuration."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved top scores", content = @Content(schema = @Schema(implementation = TopScoresListResponse.class)))
    @ApiResponse(responseCode = "404", description = "Leaderboard not found", content = @Content(schema = @Schema(hidden = true)))
    public TopScoresListResponse getTopScores(
            @Parameter(description = "The unique slug of the leaderboard", example = "global-high-scores")
            @PathVariable String slug,

            @Parameter(
                    description = "Number of top scores to return",
                    example = "10",
                    schema = @Schema(defaultValue = "10")
            )
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<ScoreEntryResponse> scores = scoreService.getTopScores(slug, limit);
        return TopScoresListResponse.fromScoreEntryResponses(scores);
    }

    @PostMapping("/{slug}/scores")
    @Operation(
            summary = "Submit a score",
            description = "Submit a new score for a player. The score will be validated against the leaderboard's min/max constraints."
    )
    @ApiResponse(responseCode = "200", description = "Score submitted successfully", content = @Content(schema = @Schema(implementation = ScoreSubmitResponse.class)))
    @ApiResponse(responseCode = "404", description = "Leaderboard not found", content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(hidden = true)))
    public ScoreSubmitResponse submitScore(
            @Parameter(description = "The unique slug of the leaderboard", example = "global-high-scores")
            @PathVariable String slug,
            @Valid @RequestBody ScoreSubmissionRequest request) {
        return scoreService.submitScore(slug, request.playerAlias(), request.scoreValue(), request.metadata());
    }

    @GetMapping("/{slug}/players/{playerAlias}")
    @Operation(
            summary = "Get player rank and surrounding scores",
            description = "Fetch a specific player's score and rank, optionally including surrounding players. Returns minimal data format with start rank and scores array."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved player score", content = @Content(schema = @Schema(implementation = PlayerScoreResponse.class, example = "{\"startRank\": 1, \"scores\": [{\"n\": \"Player1\", \"s\": 9500}, {\"n\": \"Player2\", \"s\": 8200}]}")))
    @ApiResponse(responseCode = "404", description = "Leaderboard or player not found", content = @Content(schema = @Schema(hidden = true)))
    @Schema(description = "Get player score with surrounding scores", implementation = PlayerScoreResponse.class)
    public Object getPlayerScore(
            @Parameter(description = "The unique slug of the leaderboard", example = "global-high-scores")
            @PathVariable String slug,

            @Parameter(description = "The player's alias/username", example = "PlayerOne")
            @PathVariable String playerAlias,

            @Parameter(
                    description = "Number of ranks above and below to fetch (e.g., 5 means ±5 ranks)",
                    example = "5",
                    schema = @Schema(defaultValue = "0")
            )
            @RequestParam(defaultValue = "0") int surrounding,

            @Parameter(description = "Include metadata in response", required = false)
            @RequestParam(value = "includeMetadata", defaultValue = "false") Boolean includeMetadata
    ) {
        List<ScoreEntryResponse> scores = scoreService.getPlayerScoreWithSurrounding(slug, playerAlias, surrounding);
        
        boolean includeMeta = includeMetadata != null && includeMetadata;
        
        if (includeMeta) {
            return PlayerScoreResponse.fromScoreEntryResponses(scores, true);
        } else {
            return PlayerScoreWithoutMetadataResponse.fromScoreEntryResponses(scores);
        }
    }

    @GetMapping("/{slug}/combined")
    @Operation(
            summary = "Get top scores and player score with surrounding ranks",
            description = "Combined endpoint that returns both top scores and a specific player's score with surrounding ranks."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved combined leaderboard data", content = @Content(schema = @Schema(implementation = CombinedLeaderboardResponse.class)))
    @ApiResponse(responseCode = "404", description = "Leaderboard or player not found", content = @Content(schema = @Schema(hidden = true)))
    public Object getTopAndPlayer(
            @Parameter(description = "The unique slug of the leaderboard", example = "global-high-scores")
            @PathVariable String slug,

            @Parameter(
                    description = "Number of top scores to return",
                    example = "10",
                    schema = @Schema(defaultValue = "10")
            )
            @RequestParam(defaultValue = "10") int topLimit,

            @Parameter(description = "The player's alias/username", example = "PlayerOne")
            @RequestParam String playerAlias,

            @Parameter(
                    description = "Number of ranks above and below the player to fetch (e.g., 5 means ±5 ranks)",
                    example = "5",
                    schema = @Schema(defaultValue = "0")
            )
            @RequestParam(defaultValue = "0") int surrounding,

            @Parameter(description = "Include metadata for top scores", required = false)
            @RequestParam(value = "includeMetadata", defaultValue = "false") Boolean includeMetadata
    ) {
        List<ScoreEntryResponse> topScores = scoreService.getTopScores(slug, topLimit);
        List<ScoreEntryResponse> playerScores = scoreService.getPlayerScoreWithSurrounding(slug, playerAlias, surrounding);
        
        boolean includeMeta = includeMetadata != null && includeMetadata;
        
        if (includeMeta) {
            return CombinedLeaderboardResponse.create(topScores, playerScores);
        } else {
            return CombinedLeaderboardWithoutMetadataResponse.create(topScores, playerScores);
        }
    }
}
