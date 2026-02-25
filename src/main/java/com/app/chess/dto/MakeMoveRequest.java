package com.app.chess.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MakeMoveRequest(
        @NotBlank
        @Pattern(regexp = "^[a-h][1-8]$", message = "fromSquare deve ser como e2, a7 etc.")
        String fromSquare,

        @NotBlank
        @Pattern(regexp = "^[a-h][1-8]$", message = "toSquare deve ser como e4, h8 etc.")
        String toSquare,

        @NotBlank
        @Pattern(regexp = "^(WHITE|BLACK)$", message = "playerColor deve ser WHITE ou BLACK")
        String playerColor
) {
}
