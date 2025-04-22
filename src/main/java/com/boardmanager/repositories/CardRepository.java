package com.boardmanager.repository;

import com.boardmanager.entity.Board;
import com.boardmanager.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByColumnBoard(Board board);
}
