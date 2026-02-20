package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Summary of an archived leaderboard snapshot")
public record ScoreArchiveSummary(
        @Schema(description = "The slug of the leaderboard", example = "global-high-scores")
        String leaderboardSlug,

        @Schema(description = "Label given to the archive snapshot", example = "Season 1")
        String resetLabel,

        @Schema(description = "Timestamp when the archive was created", example = "2023-10-01T12:00:00")
        LocalDateTime archivedAt,

        @Schema(description = "Number of entries in this snapshot", example = "150")
        long entryCount
) {
}