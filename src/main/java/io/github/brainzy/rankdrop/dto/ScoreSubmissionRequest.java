package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Request object for submitting a new score")
public record ScoreSubmissionRequest(
        @Schema(description = "Displayed player alias",
                example = "SpeedRunner99",
                minLength = 1,
                maxLength = 20)
        @NotBlank(message = "Player alias is required")
        @Size(min = 1, max = 20, message = "Player alias must be between 1 and 20 characters")
        String playerAlias,

        @Schema(description = "Score to be added (Supports decimals)",
                example = "1550.50")
        @NotNull(message = "Score is required")
        Double scoreValue,

        @Schema(description = "Optional metadata string associated with the score", example = "Level 5 - Warrior")
        String metadata
) {
}