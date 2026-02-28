package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Score response with metadata")
public record TopScoreWithMetadataResponse(
        @Schema(description = "Player name", example = "Player1")
        String n,

        @Schema(description = "Score value", example = "9500")
        double s,

        @Schema(description = "Metadata", example = "Sword-Level5")
        String m
) {
    public static TopScoreWithMetadataResponse fromScoreEntryResponse(ScoreEntryResponse response) {
        return new TopScoreWithMetadataResponse(response.playerAlias(), response.scoreValue(), response.metadata());
    }
}
