package com.sims.simscoreservice.stockMovement.repository;

import com.sims.simscoreservice.stockMovement.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
}
