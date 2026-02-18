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
                example = "1550.50",
                minimum = "0",
                maximum = "1000000") // ToDo update min and max from leaderboard config
        @NotNull(message = "Score is required")
        @Min(value = 0, message = "Score cannot be negative")
        @Max(value = 1000000, message = "Score exceeds the maximum limit")
        Double scoreValue
) {
}