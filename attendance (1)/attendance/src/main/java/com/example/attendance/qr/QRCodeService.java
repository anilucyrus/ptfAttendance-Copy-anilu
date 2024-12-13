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
import java.util.UUID;

@Service
public class QRCodeService {



    private static final String QR_CODE_DATA = "Attendance System"; // Or generate dynamically if required
    private static final int QR_CODE_WIDTH = 250;
    private static final int QR_CODE_HEIGHT = 250;

    private String currentQRCode;

    // Method to generate a new QR code
    private String generateQRCode() throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(QR_CODE_DATA, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] imageData = pngOutputStream.toByteArray();

        // Return a base64-encoded string for use in your application
        return java.util.Base64.getEncoder().encodeToString(imageData);
    }

    // This method runs when the application starts
    @PostConstruct
    public void generateInitialQRCode() {
        try {
            this.currentQRCode = generateQRCode();
            System.out.println("Initial QR Code Generated: " + this.currentQRCode);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }

    // Scheduled task to regenerate QR code every 10 seconds (if needed)
//    @Scheduled(fixedRate = 10000)  // 10000 ms = 10 seconds
    public void regenerateQRCode() {
        try {
            this.currentQRCode = generateQRCode();
            System.out.println("QR Code Regenerated: " + this.currentQRCode);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }

    // Get the current QR code (base64 string)
    public String getCurrentQRCode() {
        return this.currentQRCode;
    }


}
