package io.github.brainzy.rankdrop.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
public class Leaderboard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String slug;

    private String displayName;

    @JsonManagedReference
    @OneToMany(mappedBy = "leaderboard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScoreEntry> entries;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "sort_order", nullable = false)
    private SortOrder sortOrder = SortOrder.DESC;
}
