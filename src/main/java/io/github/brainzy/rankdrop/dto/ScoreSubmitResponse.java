package io.github.brainzy.rankdrop.dto;

import io.github.brainzy.rankdrop.entity.ScoreEntry;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Response object returned after a score submission")
public record ScoreSubmitResponse(
        @Schema(description = "The player's display name", example = "PlayerOne")
        String playerAlias,

        @Schema(description = "The submitted score value", example = "1500.5")
        double scoreValue,

        @Schema(description = "Timestamp when the score was submitted", example = "2023-10-01T12:00:00")
        LocalDateTime submittedAt,

        @Schema(description = "Optional metadata associated with the score", example = "Level 5 - Warrior")
        String metadata
) {
    public static ScoreSubmitResponse fromEntity(ScoreEntry entry) {
        return new ScoreSubmitResponse(
                entry.getPlayerAlias(),
                entry.getScoreValue(),
                entry.getSubmittedAt(),
                entry.getMetadata()
        );
    }
}