package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for resetting a leaderboard")
public record LeaderboardResetRequest(
        @Schema(description = "If true, current scores will be moved to the archive table before deletion.", example = "true", defaultValue = "false")
        boolean archiveScores,

        @Schema(description = "Optional label for the archived scores (e.g., 'Season 1', 'Week 42'). Required if archiveScores is true.", example = "Season 1")
        String resetLabel
) {
}