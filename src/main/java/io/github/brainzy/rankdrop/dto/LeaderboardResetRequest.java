package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for resetting a leaderboard")
public class LeaderboardResetRequest {
    @Schema(description = "If true, current scores will be moved to the archive table before deletion.", example = "true", defaultValue = "false")
    private boolean archiveScores;

    @Schema(description = "Optional label for the archived scores (e.g., 'Season 1', 'Week 42'). Required if archiveScores is true.", example = "Season 1")
    private String resetLabel;

    @AssertTrue(message = "resetLabel is required when archiveScores is true")
    public boolean isResetLabelValid() {
        return !archiveScores || (resetLabel != null && !resetLabel.trim().isEmpty());
    }
}
