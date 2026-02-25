package com.app.chess.dto;

import java.util.List;

/**
 * Board view derived from FEN.
 * - ranks: list of 8 rows (rank 8 -> 1)
 * - files: ["a","b","c","d","e","f","g","h"]
 * - grid: 8x8 where each entry is a single-character string ("P","p","R","." etc.)
 */
public record BoardResponse(
        List<String> files,
        List<Integer> ranks,
        List<List<String>> grid,
        String fen,
        String turn,
        String winner,
        String status
) {
}
