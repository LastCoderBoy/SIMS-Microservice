package com.sims.simscoreservice.stockMovement.service;


import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.stockMovement.entity.StockMovement;
import com.sims.simscoreservice.stockMovement.enums.StockMovementReferenceType;
import com.sims.simscoreservice.stockMovement.enums.StockMovementType;
import com.sims.simscoreservice.stockMovement.repository.StockMovementRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;

    @Transactional
    public void logMovement(Product product, StockMovementType type, Integer quantity,
                            String referenceId, StockMovementReferenceType referenceType, String createdBy) {
        log.info("Logging stock movement: productId={}, type={}, quantity={}, referenceId={}, referenceType={}",
                product.getProductId(), type, quantity, referenceId, referenceType);
        StockMovement movement = new StockMovement(product, quantity, type, referenceId, referenceType, createdBy);
        stockMovementRepository.save(movement);
    }
}
