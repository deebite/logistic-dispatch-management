package com.logistic.dispatch.utility;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.logistic.dispatch.entitiy.Batch;
import com.logistic.dispatch.entitiy.Pallet;
import com.logistic.dispatch.exception.QrGenerationException;
import com.logistic.dispatch.repository.BatchRepository;
import org.hibernate.annotations.CurrentTimestamp;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class QrService {

    private final ObjectMapper objectMapper;
    private final BatchRepository batchRepository;

    public QrService(ObjectMapper objectMapper, BatchRepository batchRepository) {
        this.objectMapper = objectMapper;
        this.batchRepository = batchRepository;
    }

    public void generateQrForBatch(Batch batch, List<String> serialList) {

        try {

            Map<String, Object> qrData = new HashMap<>();
            qrData.put("batchId", batch.getBatchId());
            qrData.put("batchSerial", batch.getBatchSerialNumber());
            qrData.put("productCode", batch.getProductId());
            qrData.put("totalUnits", batch.getCurrentUnits());
            qrData.put("serialNumbers", serialList);
            qrData.put("generatedAt", LocalDateTime.now().toString());

            String qrContent = objectMapper.writeValueAsString(qrData);

            int width = 400;
            int height = 400;

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, width, height);

            BufferedImage finalImage = addLabelToQr(bitMatrix, "Batch: " + batch.getBatchSerialNumber());

            String fileName = "batch_" + batch.getBatchId() + ".png";
            String folderPath = "qr-images/";

//            Path path = FileSystems.getDefault().getPath(folderPath + fileName);
            Path path = Path.of(folderPath + fileName);
            // create folder if not exists
            Files.createDirectories(path.getParent());

            ImageIO.write(finalImage, "PNG", path.toFile());

            batch.setQrCodePath(folderPath + fileName);
            batch.setQrGeneratedAt(LocalDateTime.now());
            batch.setQrStatus(QrStatus.SUCCESS);

        } catch (Exception e) {
            throw new QrGenerationException("Failed to generate QR for batch", e);
        }
    }


    public void generatePalletQr(Pallet pallet, List<String> batchSerialNumbers) {

        try {

            List<Map<String, Object>> batchDetails = new ArrayList<>();

            for (String batchSerial : batchSerialNumbers) {

                Batch batch = batchRepository.findByBatchSerialNumber(batchSerial)
                        .orElseThrow(() -> new RuntimeException("Batch not found: " + batchSerial));

                List<String> serialNumbers = batch.getProductSerialList() != null ? batch.getProductSerialList() : new ArrayList<>();

                Map<String, Object> batchMap = new HashMap<>();
                batchMap.put("batchNumber", batch.getBatchSerialNumber());
                batchMap.put("serialCount", serialNumbers.size());
                batchMap.put("serialNumbers", serialNumbers);

                batchDetails.add(batchMap);
            }

            Map<String, Object> qrData = new HashMap<>();
            qrData.put("palletNumber", pallet.getPalletSerialNumber());
            qrData.put("productId", pallet.getProductId());
            qrData.put("batchCount", pallet.getCurrentBatches());
            qrData.put("batches", batchDetails);
            qrData.put("generatedAt", LocalDateTime.now().toString());

            String qrContent = objectMapper.writeValueAsString(qrData);

            int width = 400;
            int height = 400;

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, width, height);

            BufferedImage finalImage = addLabelToQr(bitMatrix, "Pallet: " + pallet.getPalletSerialNumber());

            String fileName = "pallet_" + pallet.getPalletId() + ".png";
            String folderPath = "qr-images/";

            Path path = FileSystems.getDefault().getPath(folderPath + fileName);
            Files.createDirectories(path.getParent());

            ImageIO.write(finalImage, "PNG", path.toFile());

            pallet.setQrCodePath(folderPath + fileName);
            pallet.setQrGeneratedAt(LocalDateTime.now());
            pallet.setQrStatus(QrStatus.SUCCESS);

        } catch (Exception e) {
            throw new QrGenerationException("Failed to generate pallet QR", e);
        }
    }

    private BufferedImage addLabelToQr(BitMatrix bitMatrix, String labelText) throws Exception {
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        int qrWidth = qrImage.getWidth();
        int qrHeight = qrImage.getHeight();
        int labelHeight = 50; // extra space for text

        // Create a new image with extra height for the label
        BufferedImage finalImage = new BufferedImage(qrWidth, qrHeight + labelHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = finalImage.createGraphics();

        // White background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, qrWidth, qrHeight + labelHeight);

        // Draw QR code
        g2d.drawImage(qrImage, 0, 0, null);

        // Draw label text centered below QR
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (qrWidth - fm.stringWidth(labelText)) / 2;
        int textY = qrHeight + ((labelHeight + fm.getAscent()) / 2) - 4;
        g2d.drawString(labelText, textX, textY);

        g2d.dispose();
        return finalImage;
    }
}