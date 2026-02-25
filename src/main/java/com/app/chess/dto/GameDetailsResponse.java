package com.app.chess.dto;

import java.util.List;

public record GameDetailsResponse(
        Long id,
        String status,
        String turn,
        String winner,
        String fen,
        List<String> board8x8,
        List<MoveItem> moves
) {
    public record MoveItem(String from, String to, String color, String at) {
    }
}
