package io.github.brainzy.rankdrop.controller;

import io.github.brainzy.rankdrop.dto.ScoreSubmissionRequest;
import io.github.brainzy.rankdrop.entity.ScoreEntry;
import io.github.brainzy.rankdrop.service.ScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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
    public List<ScoreEntry> getTopScores(
            @Parameter(description = "The unique slug of the leaderboard", example = "level-1")
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
    public ScoreEntry submitScore(
            @Parameter(description = "The unique slug of the leaderboard", example = "global-high-scores")
            @PathVariable String slug,
            @Valid @RequestBody ScoreSubmissionRequest request) {
        return scoreService.submitScore(slug, request.name(), request.score());
    }
}