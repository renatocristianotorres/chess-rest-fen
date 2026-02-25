package com.app.chess.dto;

public record CreateGameResponse(Long gameId, String status, String turn, String winner, String fen) {
}
