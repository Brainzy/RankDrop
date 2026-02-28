package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Combined response containing top scores and player score with surrounding ranks (no metadata)", example = "{\"topScores\": [{\"n\": \"Player1\", \"s\": 9500}], \"playerScore\": {\"startRank\": 1, \"scores\": [{\"n\": \"Player1\", \"s\": 9500}, {\"n\": \"Player2\", \"s\": 8200}]}}")
public record CombinedLeaderboardWithoutMetadataResponse(
        @Schema(description = "List of top scores")
        List<TopScoreResponse> topScores,

        @Schema(description = "Player score with surrounding scores")
        PlayerScoreWithoutMetadataResponse playerScore
) {
    public static CombinedLeaderboardWithoutMetadataResponse create(
            List<ScoreEntryResponse> topScores,
            List<ScoreEntryResponse> playerScores
    ) {
        List<TopScoreResponse> top = topScores.stream()
                .map(TopScoreResponse::fromScoreEntryResponse)
                .toList();

        PlayerScoreWithoutMetadataResponse player = PlayerScoreWithoutMetadataResponse.fromScoreEntryResponses(playerScores);

        return new CombinedLeaderboardWithoutMetadataResponse(top, player);
    }
}
