package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Minimal response object for top scores")
public record TopScoreResponse(
        @Schema(description = "Player name", example = "Player1")
        String n,

        @Schema(description = "Score value", example = "9500")
        double s
) {
    public static TopScoreResponse fromScoreEntryResponse(ScoreEntryResponse response) {
        return new TopScoreResponse(response.playerAlias(), response.scoreValue());
    }
}
