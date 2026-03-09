package io.github.brainzy.rankdrop.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookPayload {
    private String event;
    private String leaderboard;
    private String playerAlias;
    private double score;
    private int rank;
    private String timestamp;
}
