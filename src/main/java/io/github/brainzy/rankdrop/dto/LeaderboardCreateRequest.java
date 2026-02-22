package io.github.brainzy.rankdrop.dto;

import io.github.brainzy.rankdrop.entity.ResetFrequency;
import io.github.brainzy.rankdrop.entity.ScoreStrategy;
import io.github.brainzy.rankdrop.entity.SortOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for creating a new leaderboard", example = """
        {
          "slug": "global-speedrun-v1",
          "displayName": "Global Speedrun Rankings",
          "sortOrder": "ASC",
          "scoreStrategy": "BEST_ONLY",
          "minScore": 0,
          "maxScore": 1000000,
          "resetFrequency": "NONE",
          "archiveOnReset": false
        }
        """)
public record LeaderboardCreateRequest(
        @Schema(
                description = "Unique identifier for the leaderboard. Used in API URLs.",
                example = "global-speedrun-v1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Slug is required")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must be lowercase alphanumeric with hyphens")
        String slug,

        @Schema(
                description = "The name displayed to players in-game.",
                example = "Global High Scores",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 1,
                maxLength = 50
        )
        @NotBlank(message = "Display name is required")
        @Size(min = 1, max = 50, message = "Display name must be between 1 and 50 characters")
        String displayName,

        @Schema(
                description = "Determines the ranking logic. DESC (default) for points where higher is better. ASC for time/speedruns where lower is better.",
                example = "DESC",
                defaultValue = "DESC",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = {"ASC", "DESC"}
        )
        SortOrder sortOrder,

        @Schema(
                description = "Strategy for handling score submissions. BEST_ONLY (default) keeps only the best score per player. MULTIPLE_ENTRIES stores every submission. CUMULATIVE sums all submissions of same player.",
                example = "BEST_ONLY",
                defaultValue = "BEST_ONLY",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = {"BEST_ONLY", "MULTIPLE_ENTRIES", "CUMULATIVE"}
        )
        ScoreStrategy scoreStrategy,

        @Schema(description = "Optional minimum score value allowed for submission", example = "0", defaultValue = "0")
        Double minScore,

        @Schema(description = "Optional maximum score value allowed for submission", example = "1000000", defaultValue = "1000000")
        Double maxScore,

        @Schema(
                description = "Frequency of automatic resets. NONE (default), DAILY, WEEKLY, MONTHLY.",
                example = "NONE",
                defaultValue = "NONE",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        ResetFrequency resetFrequency,

        @Schema(
                description = "If true, scores are archived before automatic reset. Default is false.",
                example = "false",
                defaultValue = "false",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        Boolean archiveOnReset
) {
    public LeaderboardCreateRequest {
        sortOrder = (sortOrder == null) ? SortOrder.DESC : sortOrder;
        scoreStrategy = (scoreStrategy == null) ? ScoreStrategy.BEST_ONLY : scoreStrategy;
        minScore = (minScore == null) ? 0.0 : minScore;
        maxScore = (maxScore == null) ? 1000000.0 : maxScore;
        resetFrequency = (resetFrequency == null) ? ResetFrequency.NONE : resetFrequency;
        archiveOnReset = (archiveOnReset != null) && archiveOnReset;
    }
}
