package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response object for player score with surrounding scores without metadata", example = "{\"startRank\": 1, \"scores\": [{\"n\": \"Player1\", \"s\": 9500}, {\"n\": \"Player2\", \"s\": 8200}]}")
public record PlayerScoreWithoutMetadataResponse(
        @Schema(description = "The rank of the first score in the list", example = "1")
        long startRank,

        @Schema(description = "List of scores without metadata")
        List<TopScoreResponse> scores
) {
    public static PlayerScoreWithoutMetadataResponse fromScoreEntryResponses(List<ScoreEntryResponse> responses) {
        long startRank = responses.get(0).rank();
        List<TopScoreResponse> scores = responses.stream()
                .map(TopScoreResponse::fromScoreEntryResponse)
                .toList();

        return new PlayerScoreWithoutMetadataResponse(startRank, scores);
    }
}
