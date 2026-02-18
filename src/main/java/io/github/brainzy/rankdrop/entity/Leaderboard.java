package io.github.brainzy.rankdrop.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

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

    @Schema(description = "Optional minimum score value allowed for submission, default is 0.", example = "0")
    private Double minScore;

    @Schema(description = "Optional maximum score value allowed for submission, default is 1000000", example = "1000000")
    private Double maxScore;
}