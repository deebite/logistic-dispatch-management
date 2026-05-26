package com.logistic.dispatch.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistic.dispatch.dto.*;
import com.logistic.dispatch.entitiy.Batch;
import com.logistic.dispatch.entitiy.Pallet;
import com.logistic.dispatch.entitiy.Product;
import com.logistic.dispatch.exception.*;
import com.logistic.dispatch.repository.BatchRepository;
import com.logistic.dispatch.repository.ProductRepository;
import com.logistic.dispatch.service.BatchService;
import com.logistic.dispatch.service.PalletService;
import com.logistic.dispatch.utility.LifeCycleStatus;
import com.logistic.dispatch.utility.ProductStatus;
import com.logistic.dispatch.utility.QrService;
import com.logistic.dispatch.utility.QrStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private static final Logger LOG = LoggerFactory.getLogger(BatchServiceImpl.class);

    public BatchServiceImpl(BatchRepository batchRepository, ProductRepository productRepository, QrService qrService, ObjectMapper objectMapper, PalletService palletService) {
        this.batchRepository = batchRepository;
        this.productRepository = productRepository;
        this.qrService = qrService;
        this.objectMapper = objectMapper;
        this.palletService = palletService;

    }

    @Override
    public ScanResponseDto scanProduct(ScanProductDto dto) {
        LOG.info("Scan Product is: {}", dto);
        Product product = validateProduct(dto.getProductCode());
        Batch batch = getOrCreateOpenBatch(product);

        List<String> serialList = batch.getProductSerialList();
        Set<String> existingSet = buildNormalizedSet(serialList);

        SerialProcessResult result =
                processSingleSerial(batch, dto.getProductSerialNumber(), serialList, existingSet);

        if (!result.isSuccess()) {
            LOG.error("Failed to process serial: {}", result.getMessage());
            throw new DuplicateSerialException(result.getMessage());
        }

        Pallet pallet = finalizeBatchAfterProcessing(batch, serialList);
        int remaining = batch.getMaxUnits() - batch.getCurrentUnits();
        String batchQrImage = null;
        String palletQrImage = null;
        String palletSerialNumber = null;

        if (batch.getStatus() == LifeCycleStatus.CLOSED) {
            batchQrImage = qrService.getQrImageBase64(batch.getQrCodePath());
        }

        if (pallet != null && pallet.getStatus() == LifeCycleStatus.CLOSED) {
            palletQrImage = qrService.getQrImageBase64(pallet.getQrCodePath());
            palletSerialNumber = pallet.getPalletSerialNumber();
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
                batchQrImage, palletSerialNumber, palletQrImage);
    }

    @Override
    public BulkScanResponseDto bulkScan(BulkScanRequestDto dto) {
        LOG.info("Bulk Scan is: {}", dto);
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

        Pallet pallet = finalizeBatchAfterProcessing(batch, serialList);
        int remaining = batch.getMaxUnits() - batch.getCurrentUnits();

        // ADD THIS BLOCK HERE
        String batchQrImage = null;;
        String palletQrImage = null;
        String palletSerialNumber = null;

        if (batch.getStatus() == LifeCycleStatus.CLOSED) {
            batchQrImage = qrService.getQrImageBase64(batch.getQrCodePath());
        }

        if (pallet != null && pallet.getStatus() == LifeCycleStatus.CLOSED) {
            palletQrImage = qrService.getQrImageBase64(pallet.getQrCodePath());
            palletSerialNumber = pallet.getPalletSerialNumber();
        }

        return new BulkScanResponseDto(batch.getBatchSerialNumber(), processedCount, results, batch.getStatus().name(), remaining, batchQrImage, palletSerialNumber, palletQrImage);
    }

    @Override
    public ManualBatchCloseResponse closeBatchManually(String batchSerialNumber) {
        LOG.info("Manually closing batch: {}", batchSerialNumber);
        Batch batch = batchRepository.findByBatchSerialNumber(batchSerialNumber)
                .orElseThrow(() -> new UserNotFoundException("Batch not found!"));

        if (batch.getStatus() == LifeCycleStatus.CLOSED) {
            throw new AlreadyClosed("Batch already closed!");
        }

        batch.setStatus(LifeCycleStatus.CLOSED);
        batch.setClosedAt(LocalDateTime.now());
        batch.setQrStatus(QrStatus.PENDING);

        qrService.generateQrForBatch(batch, batch.getProductSerialList());

        batchRepository.save(batch);

        String qrImage = qrService.getQrImageBase64(batch.getQrCodePath());
        LOG.info("Batch closed successfully. QrImage Path: {}", qrImage);
        return new ManualBatchCloseResponse("Batch closed successfully",
                batch.getBatchSerialNumber(), qrImage);
    }

    private SerialProcessResult processSingleSerial(Batch batch, String rawSerial, List<String> serialList, Set<String> existingSet) {
        LOG.info("Processing serial: {}", rawSerial);
        String normalized = rawSerial.trim().toUpperCase();

        if (existingSet.contains(normalized)) {
            LOG.warn("Serial already scanned in this batch: {}", normalized);
            return new SerialProcessResult(normalized, false, "Serial already scanned in this batch");
        }

        if (batch.getCurrentUnits() >= batch.getMaxUnits()) {
            LOG.warn("Batch is already full: {}", batch.getBatchSerialNumber());
            return new SerialProcessResult(normalized, false, "Batch is already full");
        }

        serialList.add(normalized);
        existingSet.add(normalized);
        batch.setCurrentUnits(serialList.size());
        LOG.info("Serial added successfully: {}", normalized);
        return new SerialProcessResult(normalized, true, "Serial added successfully");
    }

    private Pallet finalizeBatchAfterProcessing(Batch batch, List<String> serialList) {
        LOG.info("Finalizing batch: {} and serial list: {}", batch.getBatchSerialNumber(), serialList);
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
        Pallet pallet = null;
        if (isClosedNow) {
            pallet = palletService.assignBatchToPallet(batch);
        }
        return pallet;
    }

    private Product validateProduct(String productCode) {
        LOG.info("Product code: {}", productCode);
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            LOG.warn("Product not active in this batch: {}", product.getProductCode());
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
                return objectMapper.readValue(json, new TypeReference<List<String>>() {
                });
            }
            return new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse serial list", e);
        }
    }

    private Batch createNewBatch(Product product) {
        LOG.info("Creating new batch for product: {} with details: {}", product.getProductCode(), product);
        LocalDate today = LocalDate.now();
        String formattedDate = today.format(DateTimeFormatter.BASIC_ISO_DATE);

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        long todayCount = batchRepository.countTodayBatches(product.getProductId(), startOfDay, endOfDay);

        String sequence = String.format("%04d", todayCount + 1);

        String batchSerialNumber = "B-" + product.getManufacturerCode() + "-" + product.getProductCode() + "-" + formattedDate + "-" + sequence;

        Batch batch = new Batch();
        batch.setBatchSerialNumber(batchSerialNumber);
        batch.setProductId(product.getProductId());
        batch.setMaxUnits(product.getBoxCapacity());
        batch.setCurrentUnits(0);
        batch.setStatus(LifeCycleStatus.OPEN);
        batch.setProductSerialList(new ArrayList<>());
        LOG.info("New batch created: {}", batch);
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
            LOG.info("No batches found with PENDING QR status.");
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
                LOG.error("Error occurred while processing batch: {}", batch.getBatchSerialNumber(), e);
                results.add(new QrBatchResult(batch.getBatchSerialNumber(), false, "QR generation failed: " + e.getMessage()));
            }
        }
        LOG.info("QR processing completed. Processed {} batches.", processedCount);
        return new QrProcessResponse(true, "QR processing completed.", processedCount, results);
    }

    @Override
    public BatchQrResponseDto  reprintBatchQr(String batchSerialNumber) {
        LOG.info("Reprinting QR for batch: {}", batchSerialNumber);
        Batch batch = batchRepository.findByBatchSerialNumber(batchSerialNumber).orElseThrow(() -> new UserNotFoundException("Batch not found!"));

        if (batch.getStatus() != LifeCycleStatus.CLOSED) {
            LOG.error("Batch is not closed, cannot reprint QR: {}", batchSerialNumber);
            throw new RuntimeException("QR can only be reprinted for CLOSED batch");
        }
        String qrImage = qrService.generateBatchQrBase64(batch);
        LOG.info("Reprinted QR for batch: {} successful", batchSerialNumber);
        return new BatchQrResponseDto(batch.getBatchSerialNumber(), qrImage);
    }
}