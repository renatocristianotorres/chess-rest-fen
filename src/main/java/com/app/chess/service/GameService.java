package com.app.chess.service;

import com.app.chess.dto.BoardResponse;
import com.app.chess.dto.GameDetailsResponse;
import com.app.chess.model.Game;
import com.app.chess.model.GameStatus;
import com.app.chess.model.Move;
import com.app.chess.repository.GameRepository;
import com.app.chess.repository.MoveRepository;
import com.app.chess.service.fen.FenCodec;
import com.app.chess.service.rules.MoveRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GameService {

    private static final String INITIAL_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private final GameRepository gameRepo;
    private final MoveRepository moveRepo;

    public GameService(GameRepository gameRepo, MoveRepository moveRepo) {
        this.gameRepo = gameRepo;
        this.moveRepo = moveRepo;
    }

    private static void applyPromotionIfNeeded(char[][] board, int tr, int tc, char moving) {
         if (Character.toLowerCase(moving) != 'p') return;
        if (moving == 'P' && tr == 0) {
            board[tr][tc] = 'Q';
        } else if (moving == 'p' && tr == 7) {
            board[tr][tc] = 'q';
        }
    }

    private static boolean isKing(char p) {
        return p == 'K' || p == 'k';
    }

    public Game createGame() {
        Game g = new Game(INITIAL_FEN);
        g.setStatus(GameStatus.EM_EXECUCAO);
        g.setTurn("WHITE");
        return gameRepo.save(g);
    }

    @Transactional
    public void makeMove(Long gameId, String from, String to, String color) {
        Game game = gameRepo.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado"));

        if (!game.getStatus().equals(GameStatus.EM_EXECUCAO)) {
            throw new IllegalStateException("Não há Partida em está em andamento");
        }
        if (game.getWinner() != null) {
            throw new IllegalStateException("Partida finalizada");
        }

        // Check turn from game.turn (UI) and request color
        boolean whiteReq = "WHITE".equals(color);
        if (whiteReq && !"WHITE".equals(game.getTurn())) throw new IllegalStateException("Não é a vez de WHITE");
        if (!whiteReq && !"BLACK".equals(game.getTurn())) throw new IllegalStateException("Não é a vez de BLACK");

        // Parse FEN
        var pos = FenCodec.parse(game.getBoardState());
        char[][] board = pos.board();

        // Validate progressive rules (pawns & rooks)
        MoveRules.validatePawnOrRookMove(board, from, to, pos.turn());

        // Apply move
        int[] a = FenCodec.squareToRC(from);
        int[] b = FenCodec.squareToRC(to);

        char moving = board[a[0]][a[1]];
        char captured = board[b[0]][b[1]];

        board[a[0]][a[1]] = '.';
        board[b[0]][b[1]] = moving;

        // Phase 2: pawn promotion (auto to queen)
        applyPromotionIfNeeded(board, b[0], b[1], moving);

        // Optional: finish game if a king is captured (simple end condition)
        if (isKing(captured)) {
            game.setStatus(GameStatus.FINALIZADO);
            game.setWinner("WHITE".equals(color) ? "WHITE" : "BLACK");
        }

        boolean isPawnMove = Character.toLowerCase(moving) == 'p';
        boolean isCapture = captured != '.';

        int halfmove = (isPawnMove || isCapture) ? 0 : (pos.halfmove() + 1);
        int fullmove = pos.fullmove();
        char nextTurn = (pos.turn() == 'w') ? 'b' : 'w';
        if (pos.turn() == 'b') fullmove = fullmove + 1; // after black plays

        String newFen = FenCodec.toFen(new FenCodec.FenPosition(board, nextTurn, halfmove, fullmove));

        game.setBoardState(newFen);
        game.setTurn(nextTurn == 'w' ? "WHITE" : "BLACK");

        moveRepo.save(new Move(gameId, from, to, color));
        gameRepo.save(game);
    }

    public GameDetailsResponse getGame(Long gameId) {
        Game game = gameRepo.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));

        var pos = FenCodec.parse(game.getBoardState());
        List<String> board8x8 = FenCodec.boardTo8x8(pos.board());

        List<GameDetailsResponse.MoveItem> moves = moveRepo.findByGameIdOrderByCreatedAtAsc(gameId)
                .stream()
                .map(m -> new GameDetailsResponse.MoveItem(
                        m.getFromSquare(),
                        m.getToSquare(),
                        m.getPlayerColor(),
                        m.getCreatedAt().toString()
                ))
                .toList();

        return new GameDetailsResponse(
                game.getId(),
                game.getStatus().name(),
                game.getTurn(),
                game.getWinner(),
                game.getBoardState(),
                board8x8,
                moves
        );
    }


    public BoardResponse getBoard(Long gameId) {
        Game game = gameRepo.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));

        var pos = FenCodec.parse(game.getBoardState());

        var files = List.of("a", "b", "c", "d", "e", "f", "g", "h");
        var ranks = List.of(8, 7, 6, 5, 4, 3, 2, 1);

        return new BoardResponse(
                files,
                ranks,
                FenCodec.boardToGrid(pos.board()),
                game.getBoardState(),
                game.getTurn(),
                game.getWinner(),
                game.getStatus().name()
        );
    }

    public List<Game> listGames() {
        return gameRepo.findAll();
    }
}
