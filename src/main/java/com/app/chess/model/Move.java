package com.app.chess.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "moves")
public class Move {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long gameId;

    @Column(nullable = false, length = 2)
    private String fromSquare;

    @Column(nullable = false, length = 2)
    private String toSquare;

    /**
     * "WHITE" or "BLACK"
     */
    @Column(nullable = false, length = 5)
    private String playerColor;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Move() {
    }

    public Move(Long gameId, String fromSquare, String toSquare, String playerColor) {
        this.gameId = gameId;
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
        this.playerColor = playerColor;
    }

    public Long getId() {
        return id;
    }

    public Long getGameId() {
        return gameId;
    }

    public String getFromSquare() {
        return fromSquare;
    }

    public String getToSquare() {
        return toSquare;
    }

    public String getPlayerColor() {
        return playerColor;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
