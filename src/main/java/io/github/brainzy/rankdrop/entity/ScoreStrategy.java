package io.github.brainzy.rankdrop.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Strategy for handling score submissions")
public enum ScoreStrategy {
    @Schema(description = "Keep only the best score per player (highest for DESC, lowest for ASC)")
    BEST_ONLY,

    @Schema(description = "Store every score submission as separate entries")
    MULTIPLE_ENTRIES,

    @Schema(description = "Sum all submissions into one cumulative total per player")
    CUMULATIVE
}
