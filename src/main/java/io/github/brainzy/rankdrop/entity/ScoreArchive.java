package io.github.brainzy.rankdrop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "score_archive")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreArchive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String leaderboardSlug;
    private String playerAlias;
    private double scoreValue;
    private LocalDateTime submittedAt;
    
    @Builder.Default
    private LocalDateTime archivedAt = LocalDateTime.now();
    
    private String resetLabel;
}