package com.app.chess.service.rules;

import com.app.chess.service.fen.FenCodec;

import java.util.ArrayList;
import java.util.List;

public final class MoveRules {

    private MoveRules() {}

    public static void validateMove(char[][] board, String from, String to, char turn) {
        int[] a = FenCodec.squareToRC(from);
        int[] b = FenCodec.squareToRC(to);

        int fr = a[0], fc = a[1];
        int tr = b[0], tc = b[1];

        if (fr == tr && fc == tc) {
            throw new IllegalStateException("Origem e destino não podem ser iguais");
        }

        validatePseudoLegalMove(board, fr, fc, tr, tc, turn);

        char[][] simulated = FenCodec.copyBoard(board);
        char moving = simulated[fr][fc];
        simulated[fr][fc] = '.';
        simulated[tr][tc] = moving;
        applyPromotionIfNeeded(simulated, tr, tc, moving);

        if (isKingInCheck(simulated, turn)) {
            throw new IllegalStateException("Movimento inválido: deixaria o próprio rei em xeque");
        }
    }

    public static List<String> legalMovesFrom(char[][] board, String from, char turn) {
        int[] a = FenCodec.squareToRC(from);
        int fr = a[0], fc = a[1];

        char piece = board[fr][fc];
        if (piece == '.') return List.of();
        if (turn == 'w' && !FenCodec.isWhitePiece(piece)) return List.of();
        if (turn == 'b' && !FenCodec.isBlackPiece(piece)) return List.of();

        List<String> moves = new ArrayList<>();
        for (int tr = 0; tr < 8; tr++) {
            for (int tc = 0; tc < 8; tc++) {
                try {
                    validateMove(board, from, FenCodec.rcToSquare(tr, tc), turn);
                    moves.add(FenCodec.rcToSquare(tr, tc));
                } catch (IllegalStateException ignored) {
                }
            }
        }
        return moves;
    }

    public static boolean hasAnyLegalMove(char[][] board, char turn) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                char piece = board[r][c];
                if (piece == '.') continue;
                if (turn == 'w' && !FenCodec.isWhitePiece(piece)) continue;
                if (turn == 'b' && !FenCodec.isBlackPiece(piece)) continue;
                if (!legalMovesFrom(board, FenCodec.rcToSquare(r, c), turn).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isKingInCheck(char[][] board, char color) {
        char king = (color == 'w') ? 'K' : 'k';

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] == king) {
                    char attackerColor = (color == 'w') ? 'b' : 'w';
                    return isSquareAttacked(board, r, c, attackerColor);
                }
            }
        }
        throw new IllegalStateException("Rei não encontrado no tabuleiro");
    }

    public static boolean isCheckmate(char[][] board, char color) {
        return isKingInCheck(board, color) && !hasAnyLegalMove(board, color);
    }

    public static boolean isSquareAttacked(char[][] board, int targetR, int targetC, char attackerColor) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                char piece = board[r][c];
                if (piece == '.') continue;
                if (attackerColor == 'w' && !FenCodec.isWhitePiece(piece)) continue;
                if (attackerColor == 'b' && !FenCodec.isBlackPiece(piece)) continue;

                if (pieceAttacksSquare(board, r, c, targetR, targetC, piece)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void validatePseudoLegalMove(char[][] board, int fr, int fc, int tr, int tc, char turn) {
        char piece = board[fr][fc];
        if (piece == '.') throw new IllegalStateException("Não há peça na casa de origem");

        boolean whiteToMove = (turn == 'w');
        if (whiteToMove && !FenCodec.isWhitePiece(piece)) {
            throw new IllegalStateException("É a vez das brancas");
        }
        if (!whiteToMove && !FenCodec.isBlackPiece(piece)) {
            throw new IllegalStateException("É a vez das pretas");
        }

        char dest = board[tr][tc];
        if (dest != '.') {
            if (FenCodec.isWhitePiece(piece) && FenCodec.isWhitePiece(dest)) {
                throw new IllegalStateException("Destino ocupado por peça branca");
            }

            if (FenCodec.isBlackPiece(piece) && FenCodec.isBlackPiece(dest)) {
                throw new IllegalStateException("Destino ocupado por peça preta");
            }
        }

        validateByPiece(board, fr, fc, tr, tc, piece, dest);
    }

    private static void validateByPiece(char[][] board, int fr, int fc, int tr, int tc, char piece, char dest) {
        char pLower = Character.toLowerCase(piece);
        switch (pLower) {
            case 'p' -> validatePawn(board, fr, fc, tr, tc, piece, dest);
            case 'r' -> validateRook(board, fr, fc, tr, tc);
            case 'n' -> validateKnight(fr, fc, tr, tc);
            case 'b' -> validateBishop(board, fr, fc, tr, tc);
            case 'q' -> validateQueen(board, fr, fc, tr, tc);
            case 'k' -> validateKing(fr, fc, tr, tc);
            default -> throw new IllegalStateException("Peça inválida");
        }
    }

    private static boolean pieceAttacksSquare(char[][] board, int fr, int fc, int tr, int tc, char piece) {
        char dest = board[tr][tc];
        char pLower = Character.toLowerCase(piece);

        return switch (pLower) {
            case 'p' -> pawnAttacks(fr, fc, tr, tc, piece);
            case 'r' -> rookAttacks(board, fr, fc, tr, tc);
            case 'n' -> knightAttacks(fr, fc, tr, tc);
            case 'b' -> bishopAttacks(board, fr, fc, tr, tc);
            case 'q' -> queenAttacks(board, fr, fc, tr, tc);
            case 'k' -> kingAttacks(fr, fc, tr, tc);
            default -> false;
        };
    }

    private static boolean pawnAttacks(int fr, int fc, int tr, int tc, char piece) {
        boolean isWhite = FenCodec.isWhitePiece(piece);
        int dir = isWhite ? -1 : 1;
        return (tr - fr == dir) && Math.abs(tc - fc) == 1;
    }

    private static void validatePawn(char[][] board, int fr, int fc, int tr, int tc, char piece, char dest) {
        boolean isWhite = FenCodec.isWhitePiece(piece);
        int dir = isWhite ? -1 : 1;

        int dr = tr - fr;
        int dc = tc - fc;

        if (dc == 0) {
            if (dest != '.') throw new IllegalStateException("Peão não pode mover para frente em casa ocupada");
            if (dr == dir) return;

            boolean onStartRank = isWhite ? (fr == 6) : (fr == 1);
            if (onStartRank && dr == 2 * dir) {
                int intermediateR = fr + dir;
                if (board[intermediateR][fc] != '.') {
                    throw new IllegalStateException("Caminho bloqueado para avanço de 2 casas");
                }
                return;
            }

            throw new IllegalStateException("Movimento inválido de peão");
        }

        if (Math.abs(dc) == 1 && dr == dir) {
            if (dest == '.') throw new IllegalStateException("Peão só captura na diagonal se houver peça");
            return;
        }

        throw new IllegalStateException("Movimento inválido de peão");
    }

    private static boolean rookAttacks(char[][] board, int fr, int fc, int tr, int tc) {
        if (fr != tr && fc != tc) return false;
        return isPathClear(board, fr, fc, tr, tc);
    }

    private static void validateRook(char[][] board, int fr, int fc, int tr, int tc) {
        if (fr != tr && fc != tc) throw new IllegalStateException("Torre move apenas em linha reta");
        if (!isPathClear(board, fr, fc, tr, tc)) throw new IllegalStateException("Caminho bloqueado");
    }

    private static boolean knightAttacks(int fr, int fc, int tr, int tc) {
        int dr = Math.abs(tr - fr);
        int dc = Math.abs(tc - fc);
        return (dr == 2 && dc == 1) || (dr == 1 && dc == 2);
    }

    private static void validateKnight(int fr, int fc, int tr, int tc) {
        if (!knightAttacks(fr, fc, tr, tc)) {
            throw new IllegalStateException("Movimento inválido de cavalo");
        }
    }

    private static boolean bishopAttacks(char[][] board, int fr, int fc, int tr, int tc) {
        int dr = Math.abs(tr - fr);
        int dc = Math.abs(tc - fc);
        return dr == dc && isPathClear(board, fr, fc, tr, tc);
    }

    private static void validateBishop(char[][] board, int fr, int fc, int tr, int tc) {
        if (Math.abs(tr - fr) != Math.abs(tc - fc)) {
            throw new IllegalStateException("Bispo move apenas na diagonal");
        }
        if (!isPathClear(board, fr, fc, tr, tc)) throw new IllegalStateException("Caminho bloqueado");
    }

    private static boolean queenAttacks(char[][] board, int fr, int fc, int tr, int tc) {
        boolean rookLike = (fr == tr || fc == tc);
        boolean bishopLike = (Math.abs(tr - fr) == Math.abs(tc - fc));
        return (rookLike || bishopLike) && isPathClear(board, fr, fc, tr, tc);
    }

    private static void validateQueen(char[][] board, int fr, int fc, int tr, int tc) {
        boolean rookLike = (fr == tr || fc == tc);
        boolean bishopLike = (Math.abs(tr - fr) == Math.abs(tc - fc));

        if (!rookLike && !bishopLike) {
            throw new IllegalStateException("Dama move em linha reta ou diagonal");
        }
        if (!isPathClear(board, fr, fc, tr, tc)) throw new IllegalStateException("Caminho bloqueado");
    }

    private static boolean kingAttacks(int fr, int fc, int tr, int tc) {
        int dr = Math.abs(tr - fr);
        int dc = Math.abs(tc - fc);
        return dr <= 1 && dc <= 1;
    }

    private static void validateKing(int fr, int fc, int tr, int tc) {
        if (!kingAttacks(fr, fc, tr, tc) || (fr == tr && fc == tc)) {
            throw new IllegalStateException("Rei move apenas uma casa por vez");
        }
    }

    private static boolean isPathClear(char[][] board, int fr, int fc, int tr, int tc) {
        int stepR = Integer.compare(tr, fr);
        int stepC = Integer.compare(tc, fc);

        int r = fr + stepR;
        int c = fc + stepC;

        while (r != tr || c != tc) {
            if (board[r][c] != '.') return false;
            r += stepR;
            c += stepC;
        }
        return true;
    }

    private static void applyPromotionIfNeeded(char[][] board, int tr, int tc, char moving) {
        if (Character.toLowerCase(moving) != 'p') return;
        if (moving == 'P' && tr == 0) {
            board[tr][tc] = 'Q';
        } else if (moving == 'p' && tr == 7) {
            board[tr][tc] = 'q';
        }
    }
}
