package io.github.brainzy.rankdrop.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Determines if the leaderboard is sorted by highest score (DESC) or lowest time (ASC)")
public enum SortOrder {
    @Schema(description = "Descending: Higher values are better (e.g., Points, Kills, XP)")
    DESC,

    @Schema(description = "Ascending: Lower values are better (e.g., Speedruns, Race Times)")
    ASC
}