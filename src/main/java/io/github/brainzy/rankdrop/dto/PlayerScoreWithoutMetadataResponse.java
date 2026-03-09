package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for player score with surrounding scores without metadata",
        example = "{\"startRank\": 1, \"scores\": [{\"n\": \"Player1\", \"s\": 9500}, {\"n\": \"Player2\", \"s\": 8200}]}")
public class PlayerScoreWithoutMetadataResponse {
    @Schema(description = "The rank of the first score in the list", example = "1")
    private long startRank;

    @Schema(description = "List of scores without metadata")
    private List<TopScoreResponse> scores;

    public static PlayerScoreWithoutMetadataResponse fromScoreEntryResponses(List<ScoreEntryResponse> responses) {
        long startRank = responses.get(0).getRank();
        List<TopScoreResponse> scores = responses.stream()
                .map(TopScoreResponse::fromScoreEntryResponse)
                .toList();
        return new PlayerScoreWithoutMetadataResponse(startRank, scores);
    }
}