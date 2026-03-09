package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for submitting a new score")
public class ScoreSubmissionRequest {
    @Schema(description = "Displayed player alias",
            example = "PlayerOne",
            minLength = 1,
            maxLength = 20)
    @NotBlank(message = "Player alias is required")
    @Size(min = 1, max = 20, message = "Player alias must be between 1 and 20 characters")
    String playerAlias;

    @Schema(description = "Score to be added (Supports decimals)",
            example = "1550.50")
    @NotNull(message = "Score is required")
    Double scoreValue;

    @Schema(description = "Optional metadata string associated with the score (Max 1000 chars)", example = "Level 5 - Warrior")
    @Size(max = 1000, message = "Metadata must not exceed 1000 characters")
    String metadata;
}