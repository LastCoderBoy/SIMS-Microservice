package com.sims.simscoreservice.email;

import com.sims.simscoreservice.email.confirmationToken.entity.ConfirmationToken;
import com.sims.simscoreservice.purchaseOrder.entity.PurchaseOrder;
import com.sims.simscoreservice.email.dto.LowStockAlertDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static com.sims.common.constants.AppConstants.BASE_INVENTORY_PATH;
import static com.sims.common.constants.AppConstants.BASE_URL;

/**
 * Email Service
 * Handles all email operations using plain HTML templates
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.alert.username}")
    private String lowStockReceiver;

    @Value("${spring.mail.username}")
    private String senderEmail;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Send purchase order request email to supplier
     */
    @Async
    public void sendPurchaseOrderRequest(String supplierEmail, PurchaseOrder order, ConfirmationToken confirmationToken) {
        try {
            // Load HTML template
            String htmlTemplate = loadHtmlTemplate("templates/email/purchase-order-request.html");
            String cssStyles = loadCssStyles("static/email/css/email-styles.css");

            // Prepare data
            String token = confirmationToken.getToken();
            Map<String, String> placeholders = Map.ofEntries(
                    Map.entry("{{SUPPLIER_NAME}}", order.getSupplier().getName()),
                    Map.entry("{{PO_NUMBER}}", order.getPoNumber()),
                    Map.entry("{{ORDER_DATE}}", order.getOrderDate().format(DATE_FORMATTER)),
                    Map.entry("{{PRODUCT_NAME}}", order.getProduct().getName()),
                    Map.entry("{{PRODUCT_CATEGORY}}", order.getProduct().getCategory().toString()),
                    Map.entry("{{ORDERED_QUANTITY}}", String.valueOf(order.getOrderedQuantity())),
                    Map.entry("{{NOTES}}", order.getNotes() != null && !order.getNotes().isEmpty() ? order.getNotes() : "N/A"),
                    Map.entry("{{CONFIRM_URL}}", buildConfirmUrl(token)),
                    Map.entry("{{CANCEL_URL}}", buildCancelUrl(token)),
                    Map.entry("{{SENDER_EMAIL}}", senderEmail)
            );

            // Replace placeholders
            String htmlContent = replacePlaceholders(htmlTemplate, placeholders);

            // Inline CSS (for better email client compatibility)
            htmlContent = inlineCss(htmlContent, cssStyles);

            // Send email
            sendEmail(
                    supplierEmail,
                    "Purchase Order Request: " + order.getPoNumber() + " - " + order.getProduct().getName(),
                    htmlContent
            );

            log.info("[EMAIL-SERVICE] PO request sent to: {} for PO: {}", supplierEmail, order.getPoNumber());

        } catch (Exception e) {
            log.error("[EMAIL-SERVICE] Failed to send PO request email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send purchase order request email", e);
        }
    }

    /**
     * Send low stock alert email with multiple products
     */
    @Async
    public void sendLowStockAlert(List<LowStockAlertDto> lowStockProducts) {
        try {
            if (lowStockProducts == null || lowStockProducts.isEmpty()) {
                log.info("[EMAIL-SERVICE] No low stock products to alert");
                return;
            }

            String htmlTemplate = loadHtmlTemplate("templates/email/low-stock-alert.html");
            String cssStyles = loadCssStyles("static/email/css/email-styles.css");

            // Build table rows
            String tableRows = buildLowStockTableRows(lowStockProducts);

            // Prepare placeholders
            Map<String, String> placeholders = Map.of(
                    "{{TOTAL_COUNT}}", String.valueOf(lowStockProducts.size()),
                    "{{REPORT_DATE}}", LocalDate.now().format(DATE_FORMATTER),
                    "{{LOW_STOCK_ROWS}}", tableRows,
                    "{{DASHBOARD_URL}}", BASE_URL + BASE_INVENTORY_PATH + "/low-stock",
                    "{{SENDER_EMAIL}}", senderEmail
            );

            String htmlContent = replacePlaceholders(htmlTemplate, placeholders);
            htmlContent = inlineCss(htmlContent, cssStyles);

            // TODO: Send email to all Managers.
            sendEmail(
                    lowStockReceiver,
                    "⚠️ Daily Low Stock Alert - " + lowStockProducts.size() + " Products",
                    htmlContent
            );

            log.info("[EMAIL-SERVICE] Low stock alert sent for {} products", lowStockProducts. size());

        } catch (Exception e) {
            log.error("[EMAIL-SERVICE] Failed to send low stock alert: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send low stock alert", e);
        }
    }

    /**
     * Build HTML table rows for low stock products
     */
    private String buildLowStockTableRows(List<LowStockAlertDto> products) {
        StringBuilder rows = new StringBuilder();

        for (LowStockAlertDto product : products) {
            String stockColor = getStockLevelColor(product.getSeverity());
            String statusBadge = getStatusBadge(product.getSeverity());

            rows.append("<tr>")
                    .append("<td><strong>").append(product.getSku()).append("</strong></td>")
                    .append("<td>").append(product.getProductName()).append("</td>")
                    .append("<td>").append(product.getCategory()).append("</td>")
                    .append("<td>").append(product.getLocation()).append("</td>")
                    .append("<td style='text-align: center; color: ").append(stockColor).append("; font-weight: bold;'>")
                    .append(product.getCurrentStock()).append("</td>")
                    .append("<td style='text-align: center;'>").append(product.getMinLevel()).append("</td>")
                    .append("<td style='text-align: center;'>").append(statusBadge).append("</td>")
                    .append("</tr>");
        }

        return rows.toString();
    }

    /**
     * Get color based on severity
     */
    private String getStockLevelColor(String severity) {
        return switch (severity) {
            case "CRITICAL" -> "#dc3545"; // Red
            case "HIGH" -> "#fd7e14";     // Orange
            case "MEDIUM" -> "#ffc107";   // Yellow
            default -> "#6c757d";          // Gray
        };
    }

    /**
     * Get status badge HTML
     */
    private String getStatusBadge(String severity) {
        String color = switch (severity) {
            case "CRITICAL" -> "#dc3545";
            case "HIGH" -> "#fd7e14";
            case "MEDIUM" -> "#ffc107";
            default -> "#6c757d";
        };

        return String.format(
                "<span style='background-color: %s; color: white; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold;'>%s</span>",
                color, severity
        );
    }

    /**
     * Load HTML template from resources
     */
    private String loadHtmlTemplate(String templatePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(templatePath);
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
    }

    /**
     * Load CSS styles from resources
     */
    private String loadCssStyles(String cssPath) throws IOException {
        ClassPathResource resource = new ClassPathResource(cssPath);
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
    }

    /**
     * Replace placeholders in template
     */
    private String replacePlaceholders(String template, Map<String, String> placeholders) {
        String result = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Inline CSS into HTML (for better email client compatibility)
     */
    private String inlineCss(String html, String css) {
        // Simple approach: Add CSS in <style> tag in <head>
        return html.replace("</head>", "<style>" + css + "</style></head>");
    }

    /**
     * Send email
     */
    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(senderEmail, "SIMS Inventory System");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    /**
     * Build confirmation URL for the Supplier
     */
    private String buildConfirmUrl(String token) {
        return String.format("%s/api/v1/email/purchase-order/confirm?token=%s", BASE_URL, token);
    }

    /**
     * Build cancel URL for the Supplier
     */
    private String buildCancelUrl(String token) {
        return String.format("%s/api/v1/email/purchase-order/cancel?token=%s", BASE_URL, token);
    }
}
