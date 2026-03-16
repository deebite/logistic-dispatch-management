package com.logistic.dispatch.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistic.dispatch.dto.*;
import com.logistic.dispatch.entitiy.Batch;
import com.logistic.dispatch.entitiy.Product;
import com.logistic.dispatch.exception.DuplicateSerialException;
import com.logistic.dispatch.exception.ProductInactiveException;
import com.logistic.dispatch.exception.ProductNotFoundException;
import com.logistic.dispatch.repository.BatchRepository;
import com.logistic.dispatch.repository.ProductRepository;
import com.logistic.dispatch.service.BatchService;
import com.logistic.dispatch.service.PalletService;
import com.logistic.dispatch.utility.LifeCycleStatus;
import com.logistic.dispatch.utility.ProductStatus;
import com.logistic.dispatch.utility.QrService;
import com.logistic.dispatch.utility.QrStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final QrService qrService;
    private final ObjectMapper objectMapper;
    private final PalletService palletService;

    public BatchServiceImpl(BatchRepository batchRepository, ProductRepository productRepository, QrService qrService, ObjectMapper objectMapper, PalletService palletService) {
        this.batchRepository = batchRepository;
        this.productRepository = productRepository;
        this.qrService = qrService;
        this.objectMapper = objectMapper;
        this.palletService = palletService;

    }

    @Override
    public ScanResponseDto scanProduct(ScanProductDto dto) {

        Product product = validateProduct(dto.getProductCode());
        Batch batch = getOrCreateOpenBatch(product);

        List<String> serialList = batch.getProductSerialList();
        Set<String> existingSet = buildNormalizedSet(serialList);

        SerialProcessResult result =
                processSingleSerial(batch, dto.getProductSerialNumber(), serialList, existingSet);

        if (!result.isSuccess()) {
            throw new DuplicateSerialException(result.getMessage());
        }

        finalizeBatchAfterProcessing(batch, serialList);
        int remaining = batch.getMaxUnits() - batch.getCurrentUnits();
        String qrImage = null;

        if (batch.getStatus() == LifeCycleStatus.CLOSED) {
            qrImage = qrService.getQrImageBase64(batch.getQrCodePath());
        }

        return new ScanResponseDto(
                batch.getStatus() == LifeCycleStatus.CLOSED ? "Product scanned successfully. Batch closed."
                        : "Product scanned successfully.",
                product.getProductCode(),
                dto.getProductSerialNumber(),
                batch.getBatchSerialNumber(),
                batch.getCurrentUnits(),
                batch.getMaxUnits(),
                batch.getStatus().name(),
                remaining,
                qrImage);
    }

    @Override
    public BulkScanResponseDto bulkScan(BulkScanRequestDto dto) {

        Product product = validateProduct(dto.getProductCode());
        Batch batch = getOrCreateOpenBatch(product);

        List<String> serialList = batch.getProductSerialList();
        Set<String> existingSet = buildNormalizedSet(serialList);
        Set<String> requestUniqueSet = new HashSet<>();

        List<SerialProcessResult> results = new ArrayList<>();
        int processedCount = 0;

        for (String serial : dto.getSerialNumbers()) {

            String normalized = serial.trim().toUpperCase();

            if (!requestUniqueSet.add(normalized)) {
                results.add(new SerialProcessResult(normalized, false, "Duplicate serial in request"));
                continue;
            }

            SerialProcessResult result = processSingleSerial(batch, serial, serialList, existingSet);
            if (result.isSuccess()) {
                processedCount++;
            }

            results.add(result);
        }

        finalizeBatchAfterProcessing(batch, serialList);
        int remaining = batch.getMaxUnits() - batch.getCurrentUnits();

        // ADD THIS BLOCK HERE
        String qrImage = null;

        if (batch.getStatus() == LifeCycleStatus.CLOSED) {
            qrImage =  qrService.getQrImageBase64(batch.getQrCodePath());
        }

        return new BulkScanResponseDto(batch.getBatchSerialNumber(), processedCount, results, batch.getStatus().name(), remaining, qrImage);
    }

    private SerialProcessResult processSingleSerial(Batch batch, String rawSerial, List<String> serialList, Set<String> existingSet) {

        String normalized = rawSerial.trim().toUpperCase();

        if (existingSet.contains(normalized)) {
            return new SerialProcessResult(normalized, false, "Serial already scanned in this batch");
        }

        if (batch.getCurrentUnits() >= batch.getMaxUnits()) {
            return new SerialProcessResult(normalized, false, "Batch is already full");
        }

        serialList.add(normalized);
        existingSet.add(normalized);
        batch.setCurrentUnits(serialList.size());

        return new SerialProcessResult(normalized, true, "Serial added successfully");
    }

    private void finalizeBatchAfterProcessing(Batch batch, List<String> serialList) {

        batch.setProductSerialList(serialList);
        boolean isClosedNow = false;
        if (batch.getCurrentUnits().equals(batch.getMaxUnits())) {
            batch.setStatus(LifeCycleStatus.CLOSED);
            batch.setClosedAt(LocalDateTime.now());
            batch.setQrStatus(QrStatus.PENDING);
            qrService.generateQrForBatch(batch, serialList);

            isClosedNow = true;
        }

        batchRepository.save(batch);
        if (isClosedNow) {
            palletService.assignBatchToPallet(batch);
        }
    }

    private Product validateProduct(String productCode) {
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ProductInactiveException("Product is inactive");
        }
        return product;
    }

    private Batch getOrCreateOpenBatch(Product product) {
        return batchRepository.findByProductIdAndStatus(product.getProductId(), LifeCycleStatus.OPEN).orElseGet(() -> createNewBatch(product));
    }

    private Set<String> buildNormalizedSet(List<String> serialList) {
        return serialList.stream().map(s -> s.trim().toUpperCase()).collect(Collectors.toSet());
    }

    private String convertListToJson(List<String> serials) {
        try {
            return objectMapper.writeValueAsString(serials);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize serial list", e);
        }
    }

    private List<String> getSerialListFromJson(String json) {
        try {
            if (json != null && !json.isBlank()) {
                return objectMapper.readValue(json, new TypeReference<List<String>>() {});
            }
            return new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse serial list", e);
        }
    }

    private Batch createNewBatch(Product product) {

        LocalDate today = LocalDate.now();
        String formattedDate = today.format(DateTimeFormatter.BASIC_ISO_DATE);

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        long todayCount = batchRepository.countTodayBatches(product.getProductId(), startOfDay, endOfDay);

        String sequence = String.format("%04d", todayCount + 1);

        String batchSerialNumber = "B-" + product.getProductCode() + "-" + formattedDate + "-" + sequence;

        Batch batch = new Batch();
        batch.setBatchSerialNumber(batchSerialNumber);
        batch.setProductId(product.getProductId());
        batch.setMaxUnits(product.getBoxCapacity());
        batch.setCurrentUnits(0);
        batch.setStatus(LifeCycleStatus.OPEN);
        batch.setProductSerialList(new ArrayList<>());

        return batchRepository.save(batch);
    }

    // =====================================================
// PROCESS PENDING QR
// =====================================================

    @Override
    @Transactional(noRollbackFor = Exception.class)
    public QrProcessResponse processPendingQrBatches() {

        List<Batch> pendingBatches = batchRepository.findByQrStatus(QrStatus.PENDING);

        if (pendingBatches.isEmpty()) {
            return new QrProcessResponse(true, "No batches found with PENDING QR status.", 0, List.of());
        }

        List<QrBatchResult> results = new ArrayList<>();
        int processedCount = 0;

        for (Batch batch : pendingBatches) {
            try {
                List<String> serialList = batch.getProductSerialList();
                qrService.generateQrForBatch(batch, serialList);
                batchRepository.save(batch);

                processedCount++;

                results.add(new QrBatchResult(batch.getBatchSerialNumber(), true, "QR generated successfully"));

            } catch (Exception e) {
                results.add(new QrBatchResult(batch.getBatchSerialNumber(), false, "QR generation failed: " + e.getMessage()));
            }
        }

        return new QrProcessResponse(true, "QR processing completed.", processedCount, results);
    }

}