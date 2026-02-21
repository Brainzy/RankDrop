package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for rotating game key")
public record RotateKeyRequest(
        @Schema(description = "New game key value (minimum 16 characters)", example = "myGame_Secret_2026")
        String newGameKey
) {}
