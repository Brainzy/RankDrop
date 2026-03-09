package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing a list of top scores", example = "{\"scores\": [{\"n\": \"Player1\", \"s\": 9500}, {\"n\": \"Player2\", \"s\": 8200}]}")
public class TopScoresListResponse {
    @Schema(description = "List of top scores with minimal data")
    private List<TopScoreResponse> scores;

    public static TopScoresListResponse fromScoreEntryResponses(List<ScoreEntryResponse> responses) {
        List<TopScoreResponse> topScores = responses.stream()
                .map(TopScoreResponse::fromScoreEntryResponse)
                .toList();
        return new TopScoresListResponse(topScores);
    }
}
