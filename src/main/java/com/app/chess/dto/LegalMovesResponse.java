package com.app.chess.dto;

import java.util.List;

public record LegalMovesResponse(
        Long gameId,
        String fromSquare,
        String turn,
        boolean inCheck,
        boolean checkmate,
        List<String> legalMoves
) {}
