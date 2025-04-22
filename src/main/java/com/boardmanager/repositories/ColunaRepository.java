package com.boardmanager.repositories;

import com.boardmanager.entities.Coluna;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColumnRepository extends JpaRepository<Coluna, Long>{
}
