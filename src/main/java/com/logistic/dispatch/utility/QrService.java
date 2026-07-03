package com.logistic.dispatch.utility;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.logistic.dispatch.entitiy.Batch;
import com.logistic.dispatch.entitiy.Pallet;
import com.logistic.dispatch.entitiy.Product;
import com.logistic.dispatch.exception.QrGenerationException;
import com.logistic.dispatch.repository.BatchRepository;
import com.logistic.dispatch.repository.ProductRepository;
import com.logistic.dispatch.service.impl.BatchServiceImpl;
import org.hibernate.annotations.CurrentTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

@Component
public class QrService {

    private static final Logger LOG = LoggerFactory.getLogger(BatchServiceImpl.class);

    @Value("${qr-images.store.path}")
    private String qrCodePath;

    private final ObjectMapper objectMapper;
    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;

    public QrService(ObjectMapper objectMapper, BatchRepository batchRepository, ProductRepository productRepository) {
        this.objectMapper = objectMapper;
        this.batchRepository = batchRepository;
        this.productRepository = productRepository;
    }

    public String getQrImageBase64(String qrPath) {
        try {
            Path path = Path.of(qrPath);
            byte[] imageBytes = Files.readAllBytes(path);
            return java.util.Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            throw new QrGenerationException("Failed to read QR image", e);
        }
    }

    public void generateQrForBatch(Batch batch, List<String> serialList) {
        LOG.info("Generating QR for batch: {} with details : {}", batch.getBatchSerialNumber(), batch.toString());

        try {
            Product product = productRepository.findById(batch.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + batch.getProductId()));

            Map<String, Object> qrData = new HashMap<>();
            qrData.put("batchId", batch.getBatchId());
            qrData.put("batchSerial", batch.getBatchSerialNumber());
            qrData.put("productCode", product.getProductCode());
            qrData.put("packedBy", batch.getUpdatedBy());
            qrData.put("closedAt", batch.getClosedAt() != null ? batch.getClosedAt().toString() : null);
            qrData.put("totalUnits", batch.getCurrentUnits());
            qrData.put("serialNumbers", serialList);

            String qrContent = objectMapper.writeValueAsString(qrData);

            int width = 400;
            int height = 400;

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, width, height);

            BufferedImage finalImage = addLabelToQr(bitMatrix, "Batch: " + batch.getBatchSerialNumber());

            String fileName = "batch_" + batch.getBatchSerialNumber() + ".png";
            String folderPath = qrCodePath;

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
            Product product = productRepository.findByProductId(pallet.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + pallet.getProductId()));
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
            qrData.put("productId", product.getProductCode());
            qrData.put("batchCount", pallet.getCurrentBatches());
            qrData.put("batches", batchDetails);
            qrData.put("generatedAt", LocalDateTime.now().toString());

            String qrContent = objectMapper.writeValueAsString(qrData);

            int width = 400;
            int height = 400;

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, width, height);

            BufferedImage finalImage = addLabelToQr(bitMatrix, "Pallet: " + pallet.getPalletSerialNumber());

            String fileName = "pallet_" + pallet.getPalletSerialNumber() + ".png";
            String folderPath = qrCodePath;

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

    public String generateBatchQrBase64(Batch batch) {
        try {
            Map<String, Object> qrData = new HashMap<>();

            qrData.put("batchId", batch.getBatchId());
            qrData.put("batchSerial", batch.getBatchSerialNumber());
            qrData.put("productCode", batch.getProductId());
            qrData.put("totalUnits", batch.getCurrentUnits());
            qrData.put("serialNumbers", batch.getProductSerialList());
            qrData.put("generatedAt", LocalDateTime.now().toString());

            String qrContent = objectMapper.writeValueAsString(qrData);

            int width = 400;
            int height = 400;

            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            BitMatrix bitMatrix = qrCodeWriter.encode(
                    qrContent,
                    BarcodeFormat.QR_CODE,
                    width,
                    height);

            BufferedImage finalImage = addLabelToQr(bitMatrix, "Batch: " + batch.getBatchSerialNumber());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(finalImage, "PNG", baos);

            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {

            throw new QrGenerationException(
                    "Failed to generate batch QR",
                    e
            );
        }
    }

    public String generatePalletQrBase64(Pallet pallet) {

        try {
            List<Map<String, Object>> batchDetails = new ArrayList<>();
            for (String batchSerial : pallet.getBatchSerialList()) {

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

            String qrContent =
                    objectMapper.writeValueAsString(qrData);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 400, 400);

            BufferedImage finalImage = addLabelToQr(bitMatrix, "Pallet: " + pallet.getPalletSerialNumber());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            ImageIO.write(finalImage, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);

        } catch (Exception e) {
            throw new QrGenerationException("Failed to generate pallet QR", e);
        }
    }
}