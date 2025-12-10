package com.sims.simscoreservice.qrCode.util;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * QR Code Utility
 * Generates QR code images
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Component
public class QrCodeUtil {

    /**
     * Generate QR code image as byte array
     *
     * @param data QR code data (URL or text)
     * @param width Image width
     * @param height Image height
     * @return PNG image as bytes
     * @throws WriterException if QR code generation fails
     * @throws IOException if image writing fails
     */
    public byte[] generateQrCodeImage(String data, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
        try (ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        }
    }
}
