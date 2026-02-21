package io.github.brainzy.rankdrop.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a system configuration setting")
public class SystemSetting {
    
    @Id
    @Column(name = "setting_key", length = 100)
    @Schema(description = "The configuration key", example = "GAME_SECRET")
    private String key;
    
    @Column(name = "setting_value", columnDefinition = "TEXT")
    @Schema(description = "The configuration value", example = "gk_1234567890abcdef...")
    private String value;
    
    @UpdateTimestamp
    @Column(nullable = false, updatable = false)
    @Schema(description = "When the setting was last updated", example = "2023-10-01T12:00:00")
    private LocalDateTime updatedAt;
}
