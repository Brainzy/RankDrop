package io.github.brainzy.rankdrop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LeaderboardCreateRequest(
        @NotBlank(message = "Slug is required")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must be lowercase alphanumeric with hyphens")
        String slug,

        @NotBlank(message = "Display name is required")
        @Size(min = 3, max = 50, message = "Display name must be between 3 and 50 characters")
        String displayName
) {
}