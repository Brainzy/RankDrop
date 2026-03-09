package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for player score with surrounding scores", example = "{\"startRank\": 1, \"scores\": [{\"n\": \"Player1\", \"s\": 9500, \"m\": \"Sword-Level5\"}, {\"n\": \"Player2\", \"s\": 8200, \"m\": null}]}")
public class PlayerScoreResponse {
    @Schema(description = "The rank of the first score in the list", example = "1")
    private long startRank;

    @Schema(description = "List of scores with metadata")
    private List<TopScoreWithMetadataResponse> scores;

    public static PlayerScoreResponse fromScoreEntryResponses(List<ScoreEntryResponse> responses, boolean includeMetadata) {
        long startRank = responses.get(0).getRank();
        List<TopScoreWithMetadataResponse> scores = responses.stream()
                .map(response -> TopScoreWithMetadataResponse.fromScoreEntryResponse(response))
                .toList();
        return new PlayerScoreResponse(startRank, scores);
    }
}
