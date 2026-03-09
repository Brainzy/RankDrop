package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating an existing leaderboard")
public class LeaderboardUpdateRequest {
    @Schema(description = "New display name for the leaderboard", example = "Season 2 Rankings", minLength = 1, maxLength = 50)
    @NotBlank
    @Size(min = 1, max = 50)
    private String displayName;
}
