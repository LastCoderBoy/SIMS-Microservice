package com.sims.simscoreservice.shared.email;

import com.sims.simscoreservice.purchaseOrder.entity.PurchaseOrder;
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
import java.time.format.DateTimeFormatter;
import java.util.Map;

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

    @Value("${alert.receive.email}")
    private String lowStockReceiver;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.backend.base-url}")
    private String backendBaseUrl;

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
     * Send low stock alert email
     */
    @Async
    public void sendLowStockAlert(String productId, String productName, String sku, String category,
                                  int currentStock, int minLevel, String location) {
        try {
            String htmlTemplate = loadHtmlTemplate("templates/email/low-stock-alert.html");
            String cssStyles = loadCssStyles("static/email/css/email-styles.css");

            Map<String, String> placeholders = Map.ofEntries(
                    Map.entry("{{PRODUCT_ID}}", productId),
                    Map.entry("{{PRODUCT_NAME}}", productName),
                    Map.entry("{{SKU}}", sku),
                    Map.entry("{{CATEGORY}}", category),
                    Map.entry("{{CURRENT_STOCK}}", String.valueOf(currentStock)),
                    Map.entry("{{MIN_LEVEL}}", String.valueOf(minLevel)),
                    Map.entry("{{LOCATION}}", location),
                    Map.entry("{{DASHBOARD_URL}}", backendBaseUrl + "/inventory"),
                    Map.entry("{{SENDER_EMAIL}}", senderEmail)
            );

            String htmlContent = replacePlaceholders(htmlTemplate, placeholders);
            htmlContent = inlineCss(htmlContent, cssStyles);

            sendEmail(
                    lowStockReceiver,
                    "⚠️ Low Stock Alert: " + productName + " (" + sku + ")",
                    htmlContent
            );

            log.info("[EMAIL-SERVICE] Low stock alert sent for: {}", sku);

        } catch (Exception e) {
            log.error("[EMAIL-SERVICE] Failed to send low stock alert: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send low stock alert", e);
        }
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
        return String.format("%s/api/v1/confirmation/purchase-order/confirm? token=%s", backendBaseUrl, token);
    }

    /**
     * Build cancel URL for the Supplier
     */
    private String buildCancelUrl(String token) {
        return String.format("%s/api/v1/confirmation/purchase-order/cancel?token=%s", backendBaseUrl, token);
    }
}
