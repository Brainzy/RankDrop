package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Minimal response object for top scores")
public class TopScoreResponse {
    @Schema(description = "Player name", example = "Player1")
    private String n;

    @Schema(description = "Score value", example = "9500")
    private double s;

    public static TopScoreResponse fromScoreEntryResponse(ScoreEntryResponse response) {
        return new TopScoreResponse(response.getPlayerAlias(), response.getScoreValue());
    }
}
