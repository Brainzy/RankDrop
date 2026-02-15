package io.github.brainzy.rankdrop.dto;

import io.github.brainzy.rankdrop.entity.SortOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for creating a new leaderboard")
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
                description = "If true, a player can have multiple entries. If false (default), only their best score is kept.",
                example = "false",
                defaultValue = "false",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        Boolean allowMultipleScores
) {
    public LeaderboardCreateRequest {
        sortOrder = (sortOrder == null) ? SortOrder.DESC : sortOrder;
        allowMultipleScores = (allowMultipleScores != null) && allowMultipleScores;
    }
}