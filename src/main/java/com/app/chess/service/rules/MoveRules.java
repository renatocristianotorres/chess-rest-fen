package com.app.chess.service.rules;

import com.app.chess.service.fen.FenCodec;

public final class MoveRules {

    private MoveRules() {
    }

    /**
     * Progressive rules (phase 1):
     * - Only pawns (P/p) and rooks (R/r) can move.
     * - Pawn: 1 step forward, 2 from initial rank if clear, diagonal capture.
     * - Rook: straight lines, cannot jump over pieces.
     */
    public static void validatePawnOrRookMove(char[][] board, String from, String to, char turn) {
        int[] a = FenCodec.squareToRC(from);
        int[] b = FenCodec.squareToRC(to);

        int fr = a[0], fc = a[1];
        int tr = b[0], tc = b[1];

        char piece = board[fr][fc];
        if (piece == '.') throw new IllegalStateException("Não há peça na casa de origem");

        boolean whiteToMove = (turn == 'w');
        if (whiteToMove && !FenCodec.isWhitePiece(piece)) throw new IllegalStateException("É a vez das brancas");
        if (!whiteToMove && !FenCodec.isBlackPiece(piece)) throw new IllegalStateException("É a vez das pretas");

        char dest = board[tr][tc];
        if (dest != '.') {
            if (FenCodec.isWhitePiece(piece) && FenCodec.isWhitePiece(dest))
                throw new IllegalStateException("Destino ocupado por peça branca");
            if (FenCodec.isBlackPiece(piece) && FenCodec.isBlackPiece(dest))
                throw new IllegalStateException("Destino ocupado por peça preta");
        }

        char pLower = Character.toLowerCase(piece);
        if (pLower == 'p') {
            validatePawn(board, fr, fc, tr, tc, piece, dest);
            return;
        }
        if (pLower == 'r') {
            validateRook(board, fr, fc, tr, tc);
            return;
        }

        throw new IllegalStateException("Nesta fase, apenas peões e torres podem se mover");
    }

    private static void validatePawn(char[][] board, int fr, int fc, int tr, int tc, char piece, char dest) {
        boolean isWhite = FenCodec.isWhitePiece(piece);
        int dir = isWhite ? -1 : 1;

        int dr = tr - fr;
        int dc = tc - fc;

        // Forward (no capture)
        if (dc == 0) {
            if (dest != '.') throw new IllegalStateException("Peão não pode mover para frente em casa ocupada");

            if (dr == dir) return;

            boolean onStartRank = isWhite ? (fr == 6) : (fr == 1);
            if (onStartRank && dr == 2 * dir) {
                int intermediateR = fr + dir;
                if (board[intermediateR][fc] != '.')
                    throw new IllegalStateException("Caminho bloqueado para avanço de 2 casas");
                return;
            }

            throw new IllegalStateException("Movimento inválido de peão");
        }

        // Diagonal capture
        if (Math.abs(dc) == 1 && dr == dir) {
            if (dest == '.') throw new IllegalStateException("Peão só captura na diagonal se houver peça");
            return;
        }

        throw new IllegalStateException("Movimento inválido de peão");
    }

    private static void validateRook(char[][] board, int fr, int fc, int tr, int tc) {
        if (fr != tr && fc != tc) throw new IllegalStateException("Torre move apenas em linha reta");

        int stepR = Integer.compare(tr, fr);
        int stepC = Integer.compare(tc, fc);

        int r = fr + stepR;
        int c = fc + stepC;

        while (r != tr || c != tc) {
            if (board[r][c] != '.') throw new IllegalStateException("Caminho bloqueado para a torre");
            r += stepR;
            c += stepC;
        }
    }
}
