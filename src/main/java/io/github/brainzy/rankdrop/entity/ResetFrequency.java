package io.github.brainzy.rankdrop.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Frequency for automatic leaderboard resets")
public enum ResetFrequency {
    @Schema(description = "No automatic reset")
    NONE,

    @Schema(description = "Resets every day at 00:00 UTC")
    DAILY,

    @Schema(description = "Resets every Monday at 00:00 UTC")
    WEEKLY,

    @Schema(description = "Resets on the 1st of every month at 00:00 UTC")
    MONTHLY
}