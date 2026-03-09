package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Score response with metadata")
public class TopScoreWithMetadataResponse {
    @Schema(description = "Player name", example = "Player1")
    private String n;

    @Schema(description = "Score value", example = "9500")
    private double s;

    @Schema(description = "Metadata", example = "Sword-Level5")
    private String m;

    public static TopScoreWithMetadataResponse fromScoreEntryResponse(ScoreEntryResponse response) {
        return new TopScoreWithMetadataResponse(response.getPlayerAlias(), response.getScoreValue(), response.getMetadata());
    }
}
