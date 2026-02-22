package io.github.brainzy.rankdrop.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "leaderboards", indexes = {
        @Index(name = "idx_leaderboard_slug", columnList = "slug")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Represents a leaderboard configuration")
public class Leaderboard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Internal ID of the leaderboard", example = "1")
    private Long id;

    @Column(unique = true, nullable = false)
    @Schema(description = "Unique slug for the leaderboard", example = "global-high-scores")
    private String slug;

    @Column(nullable = false)
    @Schema(description = "Display name of the leaderboard", example = "Global High Scores")
    private String displayName;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "sort_order", nullable = false)
    @Schema(description = "Sorting order for the leaderboard", example = "DESC")
    private SortOrder sortOrder = SortOrder.DESC;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "score_strategy", nullable = false)
    @Schema(description = "Strategy for handling score submissions", example = "BEST_ONLY")
    private ScoreStrategy scoreStrategy = ScoreStrategy.BEST_ONLY;

    @Builder.Default
    @Column(name = "min_score")
    @Schema(description = "Optional minimum score value allowed for submission, default is 0.", example = "0")
    private Double minScore = 0.0;

    @Builder.Default
    @Column(name = "max_score")
    @Schema(description = "Optional maximum score value allowed for submission, default is 1000000.", example = "1000000")
    private Double maxScore = 1000000.0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "reset_frequency", nullable = false)
    @Schema(description = "Frequency of automatic resets", example = "NONE")
    private ResetFrequency resetFrequency = ResetFrequency.NONE;

    @Builder.Default
    @Column(name = "archive_on_reset", nullable = false)
    @Schema(description = "If true, scores are archived before automatic reset", example = "false")
    private boolean archiveOnReset = false;

    @Column(name = "next_reset_at")
    @Schema(description = "Timestamp of the next scheduled reset", example = "2023-11-01T00:00:00")
    private LocalDateTime nextResetAt;
}
