package io.github.brainzy.rankdrop.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "score_entries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a score entry in a leaderboard")
public class ScoreEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Internal ID of the score entry", example = "101")
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leaderboard_id", nullable = false)
    @Schema(description = "The leaderboard this score belongs to")
    private Leaderboard leaderboard;

    @NotBlank(message = "Player alias is required")
    @Schema(description = "The player's display name", example = "PlayerOne")
    private String playerAlias;

    @PositiveOrZero
    @Schema(description = "The score value", example = "1500.5")
    private double scoreValue;

    @Builder.Default
    @Schema(description = "Timestamp when the score was submitted", example = "2023-10-01T12:00:00")
    private LocalDateTime submittedAt = LocalDateTime.now();
}