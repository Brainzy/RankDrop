package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Summary of an archived leaderboard snapshot")
public class ScoreArchiveSummary {
    @Schema(description = "The slug of the leaderboard", example = "global-high-scores")
    private String leaderboardSlug;

    @Schema(description = "Label given to the archive snapshot", example = "Season 1")
    private String resetLabel;

    @Schema(description = "Timestamp when the archive was created", example = "2023-10-01T12:00:00")
    private LocalDateTime archivedAt;

    @Schema(description = "Number of entries in this snapshot", example = "150")
    private long entryCount;
}
