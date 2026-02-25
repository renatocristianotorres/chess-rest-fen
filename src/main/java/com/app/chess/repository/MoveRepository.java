package com.app.chess.repository;

import com.app.chess.model.Move;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoveRepository extends JpaRepository<Move, Long> {
    List<Move> findByGameIdOrderByCreatedAtAsc(Long gameId);
}
