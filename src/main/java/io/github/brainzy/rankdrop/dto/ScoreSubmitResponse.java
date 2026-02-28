package io.github.brainzy.rankdrop.dto;

import io.github.brainzy.rankdrop.entity.ScoreEntry;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object returned after a score submission", example = "{\"rank\": 13, \"score\": 1550.5}")
public record ScoreSubmitResponse(
        @Schema(description = "The rank of the submitted score", example = "13")
        long rank,

        @Schema(description = "The submitted score value", example = "1550.5")
        double score
) {
    public static ScoreSubmitResponse fromEntity(ScoreEntry entry, long rank) {
        return new ScoreSubmitResponse(rank, entry.getScoreValue());
    }
}
