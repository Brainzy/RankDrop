package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Combined response containing top scores and player score with surrounding ranks", example = "{\"topScores\": [{\"n\": \"Player1\", \"s\": 9500}], \"playerScore\": {\"startRank\": 1, \"scores\": [{\"n\": \"Player1\", \"s\": 9500, \"m\": \"Sword-Level5\"}, {\"n\": \"Player2\", \"s\": 8200, \"m\": null}]}}")
public record CombinedLeaderboardResponse(
        @Schema(description = "List of top scores without metadata")
        List<TopScoreResponse> topScores,

        @Schema(description = "Player score with surrounding scores")
        PlayerScoreResponse playerScore
) {
    public static CombinedLeaderboardResponse create(
            List<ScoreEntryResponse> topScores,
            List<ScoreEntryResponse> playerScores
    ) {
        List<TopScoreResponse> top = topScores.stream()
                .map(TopScoreResponse::fromScoreEntryResponse)
                .toList();

        List<TopScoreWithMetadataResponse> playerScoresWithMetadata = playerScores.stream()
                .map(TopScoreWithMetadataResponse::fromScoreEntryResponse)
                .toList();

        long startRank = playerScores.get(0).rank();
        PlayerScoreResponse player = new PlayerScoreResponse(startRank, playerScoresWithMetadata);

        return new CombinedLeaderboardResponse(top, player);
    }
}
