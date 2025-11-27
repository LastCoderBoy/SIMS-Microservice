package com.sims.simscoreservice.product.helper;

import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.product.enums.ProductStatus;
import com.sims.simscoreservice.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Product Status Modifier
 * Helper for updating product status during order processing
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductStatusModifier {

    private final ProductRepository productRepository;

    /**
     * Update product status when order is received
     * Changes ON_ORDER -> ACTIVE when product arrives
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void updateIncomingProductStatusInPm(Product orderedProduct) {
        if (orderedProduct.getStatus() == ProductStatus.ON_ORDER) {
            orderedProduct.setStatus(ProductStatus.ACTIVE);
            productRepository.save(orderedProduct);

            log.info("[PRODUCT-STATUS] Product {} status changed: ON_ORDER -> ACTIVE",
                    orderedProduct.getProductId());
        }
    }
}
