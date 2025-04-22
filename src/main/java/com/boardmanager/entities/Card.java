package com.boardmanager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private LocalDateTime creationDate = LocalDateTime.now();
    private boolean blocked = false;

    private String blockReason;
    private String unblockReason;
    private LocalDateTime blockedAt;
    private LocalDateTime unblockedAt;

    @ManyToOne
    private Column column;

    @ElementCollection
    private List<ColumnMovement> movements = new ArrayList<>();

}
