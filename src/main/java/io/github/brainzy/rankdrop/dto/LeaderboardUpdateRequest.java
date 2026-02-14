package io.github.brainzy.rankdrop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LeaderboardUpdateRequest(
        @NotBlank(message = "Display name cannot be empty")
        @Size(min = 3, max = 50)
        String displayName
) {
}