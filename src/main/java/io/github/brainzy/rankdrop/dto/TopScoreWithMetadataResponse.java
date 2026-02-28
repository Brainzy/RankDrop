package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Minimal score response with optional metadata")
public record TopScoreWithMetadataResponse(
        @Schema(description = "Player name", example = "Player1")
        String n,

        @Schema(description = "Score value", example = "9500")
        double s,

        @Schema(description = "Optional metadata", example = "Sword-Level5")
        String m
) {
    public static TopScoreWithMetadataResponse fromScoreEntryResponse(ScoreEntryResponse response, boolean includeMetadata) {
        if (includeMetadata && response.metadata() != null) {
            return new TopScoreWithMetadataResponse(response.playerAlias(), response.scoreValue(), response.metadata());
        } else {
            return new TopScoreWithMetadataResponse(response.playerAlias(), response.scoreValue(), null);
        }
    }
}
