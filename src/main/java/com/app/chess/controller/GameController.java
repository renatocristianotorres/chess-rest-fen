package com.app.chess.controller;

import com.app.chess.dto.BoardResponse;
import com.app.chess.dto.CreateGameResponse;
import com.app.chess.dto.GameDetailsResponse;
import com.app.chess.dto.MakeMoveRequest;
import com.app.chess.model.Game;
import com.app.chess.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService service;

    public GameController(GameService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateGameResponse createGame() {
        Game g = service.createGame();
        return new CreateGameResponse(g.getId(), g.getStatus().name(), g.getTurn(), g.getWinner(), g.getBoardState());
    }

    @GetMapping("/{id}")
    public GameDetailsResponse getGame(@PathVariable Long id) {
        return service.getGame(id);
    }

    @GetMapping("/{id}/board")
    public BoardResponse getBoard(@PathVariable Long id) {
        return service.getBoard(id);
    }

    @GetMapping
    public List<Game> listGames() {
        return service.listGames();
    }

    @PostMapping("/{id}/moves")
    @ResponseStatus(HttpStatus.CREATED)
    public void makeMove(@PathVariable Long id, @Valid @RequestBody MakeMoveRequest req) {
        service.makeMove(id, req.fromSquare(), req.toSquare(), req.playerColor());
    }
}
