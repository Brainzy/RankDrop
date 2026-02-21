package io.github.brainzy.rankdrop.dto;

public record WebhookPayload(
        String event,
        String leaderboard,
        String playerAlias,
        double score,
        int rank,
        String timestamp
) {}
