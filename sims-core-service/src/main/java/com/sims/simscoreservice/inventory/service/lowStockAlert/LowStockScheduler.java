package com.sims.simscoreservice.inventory.service.lowStockAlert;


import com.sims.simscoreservice.inventory.entity.Inventory;
import com.sims.simscoreservice.inventory.queryService.InventoryQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.sims.common.constants.AppConstants.DEFAULT_SORT_BY;
import static com.sims.common.constants.AppConstants.DEFAULT_SORT_DIRECTION;

@Service
@Slf4j
@RequiredArgsConstructor
public class LowStockScheduler {

    private final InventoryQueryService inventoryQueryService;
    // TODO:
    // private final EmailSender emailSender;


    //    @Scheduled(cron = "*/30 * * * * ?")
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendDailyLowStockAlert() {
        List<Inventory> lowStockProducts =
                inventoryQueryService.getAllLowStockProducts(DEFAULT_SORT_BY, DEFAULT_SORT_DIRECTION);
        if (lowStockProducts.isEmpty()) {
            return; // nothing to send
        }
        log.info("Sending daily low stock alerts product size {}.", lowStockProducts.size());
        String html = buildLowStockHtml(lowStockProducts);
        // TODO:
        // emailSender.sendLowStockEmail( "Daily Low Stock Alert", html);
    }

    public String buildLowStockHtml(List<Inventory> lowStockProducts) {
        StringBuilder html = new StringBuilder();
        html.append("<h2 style='color:#d9534f;'>Low Stock Alert - SIMS Inventory</h2>");
        html.append("<p>The following products are below the minimum stock level:</p>");
        html.append("<table border='1' cellpadding='8' cellspacing='0' style='border-collapse:collapse;'>");
        html.append("<tr style='background-color:#f2f2f2;'><th>SKU</th><th>Product Name</th><th>Category</th><th>Stock</th><th>Min Level</th></tr>");

        for (Inventory product : lowStockProducts) {
            html.append("<tr>")
                    .append("<td>").append(product.getSku()).append("</td>")
                    .append("<td>").append(product.getProduct().getName()).append("</td>")
                    .append("<td>").append(product.getProduct().getCategory()).append("</td>")
                    .append("<td>").append(product.getCurrentStock()).append("</td>")
                    .append("<td>").append(product.getMinLevel()).append("</td>")
                    .append("</tr>");
        }

        html.append("</table>");
        html.append("<p style='margin-top:20px;'>Please restock as soon as possible.</p>");
        return html.toString();
    }

}
