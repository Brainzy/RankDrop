package io.github.brainzy.rankdrop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "players", uniqueConstraints = {
        @UniqueConstraint(columnNames = "player_alias")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_alias", nullable = false, unique = true)
    private String playerAlias;

    @Column(name = "banned")
    @Builder.Default
    private boolean banned = false;

    @Column(name = "banned_at")
    private LocalDateTime bannedAt;

    @Column(name = "banned_reason")
    private String bannedReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
