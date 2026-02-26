package com.app.chess.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private GameStatus status = GameStatus.AGUARDANDO_JOGADORES;

    /**
     * String com posições iniciais FEN.
     * Examplo
     * rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
     */
    @Lob
    @Column(nullable = false)
    private String boardState;

    /**
     * "PEÇA BRANCA" ou "PRETA" para facilitar futura implementação frontend .
     */
    @Column(nullable = false)
    private String turn = "WHITE";

    /**
     * String que guarda quem ganhou: BRANCAS ou PRETAS
     */
    private String winner;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Game() {
    }

    public Game(String fen) {
        this.boardState = fen;
        this.turn = "WHITE";
        this.status = GameStatus.EM_EXECUCAO;
    }

    public Long getId() {
        return id;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public String getBoardState() {
        return boardState;
    }

    public void setBoardState(String boardState) {
        this.boardState = boardState;
    }

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }
}
