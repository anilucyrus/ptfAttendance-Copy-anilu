package com.example.attendance.qr;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@Service
public class QRCodeService {


    private static final int QR_CODE_WIDTH = 250;
    private static final int QR_CODE_HEIGHT = 250;

    private String currentQRCode; // Variable to store the QR code

    // Generate QR Code as a Base64 string
    public String generateQRCodeAsString(String qrContent) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);

        // Convert QR code to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        byte[] qrImageData = outputStream.toByteArray();

        // Convert byte array to Base64 string
        return Base64.getEncoder().encodeToString(qrImageData);
    }

    // Method to generate a QR code with the current date and time
    private String generateCurrentQRCode() throws WriterException, IOException {
        // Get current date and time
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String formattedTime = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        // Generate QR content
        String qrContent = "date:" + formattedDate + "  time:" + formattedTime;

        return generateQRCodeAsString(qrContent);
    }

    // Automatically generate QR code at startup
    @PostConstruct
    public void generateInitialQRCode() {
        try {
            this.currentQRCode = generateCurrentQRCode();
            System.out.println("Initial QR Code Generated: " + this.currentQRCode);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }

    // Getter for the current QR code
    public String getCurrentQRCode() {
        return this.currentQRCode;
    }
}
