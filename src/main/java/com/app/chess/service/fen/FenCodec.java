package com.app.chess.service.fen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class FenCodec {

    private FenCodec() {
    }

    public static FenPosition parse(String fen) {
        String[] parts = fen.trim().split("\\s+");
        if (parts.length < 6) throw new IllegalArgumentException("FEN inválida");

        String placement = parts[0];
        char turn = parts[1].charAt(0); // 'w' ou 'b'
        int halfmove = Integer.parseInt(parts[4]);
        int fullmove = Integer.parseInt(parts[5]);

        char[][] board = emptyBoard();

        String[] ranks = placement.split("/");
        if (ranks.length != 8) throw new IllegalArgumentException("FEN inválida: ranks != 8");

        for (int r = 0; r < 8; r++) {
            String rank = ranks[r];
            int file = 0;
            for (int i = 0; i < rank.length(); i++) {
                char c = rank.charAt(i);
                if (Character.isDigit(c)) {
                    file += (c - '0');
                } else {
                    if (file >= 8) throw new IllegalArgumentException("FEN inválida: coluna excedida");
                    board[r][file] = c;
                    file++;
                }
            }
            if (file != 8) throw new IllegalArgumentException("FEN inválida: rank incompleta");
        }

        if (turn != 'w' && turn != 'b') throw new IllegalArgumentException("FEN inválida: turno deve ser w ou b");

        return new FenPosition(board, turn, halfmove, fullmove);
    }

    public static String toFen(FenPosition pos) {
        StringBuilder sb = new StringBuilder();

        for (int r = 0; r < 8; r++) {
            int empty = 0;
            for (int f = 0; f < 8; f++) {
                char p = pos.board[r][f];
                if (p == '.') {
                    empty++;
                } else {
                    if (empty > 0) {
                        sb.append(empty);
                        empty = 0;
                    }
                    sb.append(p);
                }
            }
            if (empty > 0) sb.append(empty);
            if (r < 7) sb.append('/');
        }

        sb.append(' ').append(pos.turn);
        // Nesta fase ainda não implementamos roque/en-passant:
        sb.append(" - - ");
        sb.append(pos.halfmove).append(' ').append(pos.fullmove);

        return sb.toString();
    }

    public static char[][] emptyBoard() {
        char[][] b = new char[8][8];
        for (char[] row : b) Arrays.fill(row, '.');
        return b;
    }

    public static int fileIndex(char file) {
        return file - 'a';
    }     // 'a'..'h'

    public static int rankIndex(char rank) {
        return 8 - (rank - '0');
    } // '1'..'8' => 7..0

    public static int[] squareToRC(String sq) {
        if (sq == null || sq.length() != 2) throw new IllegalArgumentException("Casa inválida");
        int c = fileIndex(sq.charAt(0));
        int r = rankIndex(sq.charAt(1));
        if (r < 0 || r > 7 || c < 0 || c > 7) throw new IllegalArgumentException("Casa fora do tabuleiro");
        return new int[]{r, c};
    }

    public static boolean isWhitePiece(char p) {
        return Character.isUpperCase(p);
    }

    public static boolean isBlackPiece(char p) {
        return Character.isLowerCase(p);
    }

    /**
     * Board for frontend: 8 strings (rank 8 to 1), each 8 chars using piece letters or '.'
     */
    public static List<String> boardTo8x8(char[][] board) {
        List<String> out = new ArrayList<>(8);
        for (int r = 0; r < 8; r++) {
            out.add(new String(board[r]));
        }
        return out;
    }

    /**
     * Grid for frontend: 8x8 list of strings (rank 8->1, file a->h).
     */
    public static List<List<String>> boardToGrid(char[][] board) {
        List<List<String>> grid = new ArrayList<>(8);
        for (int r = 0; r < 8; r++) {
            List<String> row = new ArrayList<>(8);
            for (int c = 0; c < 8; c++) {
                row.add(String.valueOf(board[r][c]));
            }
            grid.add(row);
        }
        return grid;
    }

    public record FenPosition(char[][] board, char turn, int halfmove, int fullmove) {
    }

}
