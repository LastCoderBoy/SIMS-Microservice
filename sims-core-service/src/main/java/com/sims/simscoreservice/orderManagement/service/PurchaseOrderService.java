package com.sims.simscoreservice.orderManagement.service;


import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.purchaseOrder.dto.DetailsPurchaseOrderView;
import com.sims.simscoreservice.purchaseOrder.dto.PurchaseOrderRequest;
import com.sims.simscoreservice.purchaseOrder.dto.SummaryPurchaseOrderView;
import com.sims.simscoreservice.purchaseOrder.enums.PurchaseOrderStatus;
import org.apache.coyote.BadRequestException;

public interface PurchaseOrderService {
    ApiResponse<PurchaseOrderRequest> createPurchaseOrder(PurchaseOrderRequest stockRequestDto,
                                                          String jwtToken) throws BadRequestException;
    PaginatedResponse<SummaryPurchaseOrderView> getAllPurchaseOrders(int page, int size, String sortBy, String sortDirection);
    DetailsPurchaseOrderView getDetailsForPurchaseOrder(Long orderId);
    PaginatedResponse<SummaryPurchaseOrderView> searchPurchaseOrders(String text, int page, int size, String sortBy, String sortDirection);
    PaginatedResponse<SummaryPurchaseOrderView> filterPurchaseOrders(ProductCategories category, PurchaseOrderStatus status, String sortBy, String sortDirection, int page, int size);
}
