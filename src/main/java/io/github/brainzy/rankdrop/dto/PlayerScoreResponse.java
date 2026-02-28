package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response object for player score with surrounding scores", example = "{\"startRank\": 1, \"scores\": [{\"n\": \"Player1\", \"s\": 9500}, {\"n\": \"Player2\", \"s\": 8200}]}")
public record PlayerScoreResponse(
        @Schema(description = "The rank of the first score in the list", example = "1")
        long startRank,

        @Schema(description = "List of scores with minimal data")
        List<TopScoreWithMetadataResponse> scores
) {
    public static PlayerScoreResponse fromScoreEntryResponses(List<ScoreEntryResponse> responses, boolean includeMetadata) {
        if (responses.isEmpty()) {
            return new PlayerScoreResponse(0, List.of());
        }

        long startRank = responses.get(0).rank();
        List<TopScoreWithMetadataResponse> scores = responses.stream()
                .map(response -> TopScoreWithMetadataResponse.fromScoreEntryResponse(response, includeMetadata))
                .toList();

        return new PlayerScoreResponse(startRank, scores);
    }
}
