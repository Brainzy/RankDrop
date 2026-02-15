package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Request object for submitting a new score")
public record ScoreSubmissionRequest(
        @Schema(description = "The display name of the player",
                example = "SpeedRunner99",
                minLength = 2,
                maxLength = 20)
        @NotBlank(message = "Player name is required")
        @Size(min = 2, max = 20, message = "Name must be between 2 and 20 characters")
        String name,

        @Schema(description = "Score to be added (Supports decimals)",
                example = "1550.50",
                minimum = "0",
                maximum = "1000000")
        @NotNull(message = "Score is required")
        @Min(value = 0, message = "Score cannot be negative")
        @Max(value = 1000000, message = "Score exceeds the maximum limit")
        Double score
) {
}