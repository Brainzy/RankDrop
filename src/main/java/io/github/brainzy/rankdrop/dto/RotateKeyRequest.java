package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for rotating game key")
public class RotateKeyRequest {
    @Schema(description = "New game key value (minimum 16 characters)", example = "myGame_Secret_2026")
    private String newGameKey;
}
