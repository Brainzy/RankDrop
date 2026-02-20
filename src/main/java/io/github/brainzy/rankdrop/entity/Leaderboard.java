package io.github.brainzy.rankdrop.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "leaderboards")
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

    @Schema(description = "Display name of the leaderboard", example = "Global High Scores")
    private String displayName;

    @JsonManagedReference
    @OneToMany(mappedBy = "leaderboard", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "List of score entries associated with this leaderboard")
    private List<ScoreEntry> entries;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "sort_order", nullable = false)
    @Schema(description = "Sorting order for the leaderboard", example = "DESC")
    private SortOrder sortOrder = SortOrder.DESC;

    @Builder.Default
    @Column(name = "allow_multiple_scores", nullable = false)
    @Schema(description = "If true, a player can have multiple entries. If false, only their best score is kept.", example = "false")
    private boolean allowMultipleScores = false;

    @Builder.Default
    @Column(name = "is_cumulative", nullable = false)
    @Schema(description = "If true, new scores are added to the player's existing total. If false, only the best score is kept.", example = "false")
    private boolean isCumulative = false;

    @Builder.Default
    @Schema(description = "Optional minimum score value allowed for submission, default is 0.", example = "0")
    private Double minScore = 0.0;

    @Builder.Default
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

    @Schema(description = "Timestamp of the next scheduled reset", example = "2023-11-01T00:00:00")
    private LocalDateTime nextResetAt;
}