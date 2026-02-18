package io.github.brainzy.rankdrop.dto;

import io.github.brainzy.rankdrop.entity.ScoreEntry;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Response object representing a score entry with rank")
public record ScoreEntryResponse(
        @Schema(description = "The player's display name", example = "PlayerOne")
        String playerAlias,

        @Schema(description = "The score value", example = "1500.5")
        double scoreValue,

        @Schema(description = "The rank of the player in the leaderboard", example = "1")
        long rank,

        @Schema(description = "Timestamp when the score was submitted", example = "2023-10-01T12:00:00")
        LocalDateTime submittedAt
) {
    public static ScoreEntryResponse fromEntity(ScoreEntry entry, long rank) {
        return new ScoreEntryResponse(
                entry.getPlayerAlias(),
                entry.getScoreValue(),
                rank,
                entry.getSubmittedAt()
        );
    }
}