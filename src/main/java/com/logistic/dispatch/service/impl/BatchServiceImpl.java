package com.logistic.dispatch.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistic.dispatch.dto.*;
import com.logistic.dispatch.entitiy.*;
import com.logistic.dispatch.exception.*;
import com.logistic.dispatch.repository.BatchRepository;
import com.logistic.dispatch.repository.GrtReportRepository;
import com.logistic.dispatch.repository.ProductRepository;
import com.logistic.dispatch.repository.UserInfoRepository;
import com.logistic.dispatch.service.BatchService;
import com.logistic.dispatch.service.PalletService;
import com.logistic.dispatch.utility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final UserInfoRepository userInfoRepository;
    private final GrtReportRepository grtReportRepository;
    private final QrService qrService;
    private final ObjectMapper objectMapper;
    private final PalletService palletService;
    private static final Logger LOG = LoggerFactory.getLogger(BatchServiceImpl.class);

    public BatchServiceImpl(BatchRepository batchRepository, ProductRepository productRepository, UserInfoRepository userInfoRepository, GrtReportRepository grtReportRepository, QrService qrService, ObjectMapper objectMapper, PalletService palletService) {
        this.batchRepository = batchRepository;
        this.productRepository = productRepository;
        this.userInfoRepository = userInfoRepository;
        this.grtReportRepository = grtReportRepository;
        this.qrService = qrService;
        this.objectMapper = objectMapper;
        this.palletService = palletService;

    }

    @Override
    public ScanResponseDto scanProduct(ScanProductDto dto) {
        LOG.info("Scan Product is: {}", dto);
        Product product = validateProduct(dto.getProductCode());
        validateGrtReport(product, dto.getProductSerialNumber());
        LOG.info("GRT Validation complete for product: {}", dto.getProductSerialNumber());
        Batch batch = getOrCreateBatchForUser(product);

        List<String> serialList = batch.getProductSerialList();
        Set<String> existingSet = buildNormalizedSet(serialList);

        SerialProcessResult result = processSingleSerial(batch, dto.getProductSerialNumber(), serialList, existingSet);

        if (!result.isSuccess()) {
            LOG.error("Failed to process serial: {}", result.getMessage());
            throw new DuplicateSerialException(result.getMessage());
        }

        Pallet pallet = finalizeBatchAfterProcessing(batch, serialList);
        int remaining = batch.getMaxUnits() - batch.getCurrentUnits();
        String batchQrImage = null;
        String palletQrImage = null;
        String palletSerialNumber = null;
        String palletStatus = null;
        String currentBatches = null;
        String maxBatches = null;

        if (batch.getStatus() == LifeCycleStatus.CLOSED) {
            batchQrImage = qrService.getQrImageBase64(batch.getQrCodePath());
        }

        if (pallet != null) {
            palletSerialNumber = pallet.getPalletSerialNumber();
            palletStatus = pallet.getStatus() != null ? pallet.getStatus().name() : null;
            currentBatches = pallet.getCurrentBatches() != null ? pallet.getCurrentBatches().toString() : null;
            maxBatches = pallet.getMaxBatches() != null ? pallet.getMaxBatches().toString() : null;

            if (pallet.getStatus() == LifeCycleStatus.CLOSED) {
                palletQrImage = qrService.getQrImageBase64(pallet.getQrCodePath());
            }
        }

        assert pallet != null;
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
                batchQrImage, palletSerialNumber,
                palletStatus,
                currentBatches,
                maxBatches,
                palletQrImage);
    }

    @Override
    public BulkScanResponseDto bulkScan(BulkScanRequestDto dto) {
        LOG.info("Bulk Scan is: {}", dto);
        Product product = validateProduct(dto.getProductCode());
        Batch batch = getOrCreateBatchForUser(product);

        List<String> serialList = batch.getProductSerialList();
        Set<String> existingSet = buildNormalizedSet(serialList);
        Set<String> requestUniqueSet = new HashSet<>();

        List<SerialProcessResult> results = new ArrayList<>();
        int processedCount = 0;

        for (String serial : dto.getSerialNumbers()) {
            validateGrtReport(product, serial);
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
        String batchQrImage = null;
        ;
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
    @Transactional
    public ManualBatchCloseResponse closeBatch(String batchSerialNumber) {

        LOG.info("Manual batch close requested for batch : {}", batchSerialNumber);

        UserInfo user = getLoggedInUser();
        Batch batch = batchRepository.findByBatchSerialNumber(batchSerialNumber)
                .orElseThrow(() -> new ProductNotFoundException("Batch not found."));

        // Already Closed
        if (batch.getStatus() == LifeCycleStatus.CLOSED) {
            throw new AlreadyClosed("Batch is already closed.");
        }

        //In progress
        if (batch.getStatus() == LifeCycleStatus.IN_PROGRESS) {
            // Only owner can close
            if (!user.getUserId().equals(batch.getAssignedUserId())) {
                throw new BatchException("Only the assigned operator can close this batch.");
            }
        }

        // AVAILABLE
        else if (batch.getStatus() == LifeCycleStatus.AVAILABLE) {
            // Only Supervisor/Admin
            if (user.getRole() != UserRole.SUPERVISOR && user.getRole() != UserRole.ADMIN) {
                throw new BatchException("Only Supervisor or Admin can close an available batch.");
            }
            batch.setAssignedUserId(user.getUserId());
            batch.setAssignedUserName(user.getUsername());
            batch.setAssignedAt(LocalDateTime.now());
        }
        batch.setStatus(LifeCycleStatus.CLOSED);
        batch.setClosedAt(LocalDateTime.now());
        batch.setQrStatus(QrStatus.PENDING);

        Batch savedBatch = batchRepository.save(batch);
        // Generate QR
        qrService.generateQrForBatch(savedBatch, savedBatch.getProductSerialList());
        String qrImage = qrService.getQrImageBase64(savedBatch.getQrCodePath());

        LOG.info("Batch {} closed successfully by {}", savedBatch.getBatchSerialNumber(), user.getUsername());

        return new ManualBatchCloseResponse("Batch closed successfully.",
                savedBatch.getBatchSerialNumber(), qrImage);
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

    private Batch getOrCreateBatchForUser(Product product) {

        String username = SecurityUtils.getCurrentUsername();

        UserInfo user = userInfoRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Step 1 : Check if the current user already has an active batch
        Optional<Batch> assignedBatch = batchRepository.findByProductIdAndAssignedUserIdAndStatus(product.getProductId(), user.getUserId(), LifeCycleStatus.IN_PROGRESS);

        if (assignedBatch.isPresent()) {
            return assignedBatch.get();
        }

        // Step 2 : Check for an AVAILABLE batch
        Optional<Batch> availableBatch = batchRepository.findFirstByProductIdAndAssignedUserIdIsNullAndStatus(product.getProductId(), LifeCycleStatus.AVAILABLE);

        if (availableBatch.isPresent()) {

            Batch batch = availableBatch.get();

            batch.setAssignedUserId(user.getUserId());
            batch.setAssignedUserName(user.getUsername());
            batch.setAssignedAt(LocalDateTime.now());
            batch.setStatus(LifeCycleStatus.IN_PROGRESS);

            return batchRepository.save(batch);
        }

        // Step 3 : Create a new batch
        return createAndAssignBatch(product, user);
    }

    private Set<String> buildNormalizedSet(List<String> serialList) {
        return serialList.stream().map(s -> s.trim().toUpperCase()).collect(Collectors.toSet());
    }

    private Batch createAndAssignBatch(Product product, UserInfo user) {
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
        batch.setProductSerialList(new ArrayList<>());

        assignBatchToUser(batch, user);

        LOG.info("New batch created and assigned to user {} : {}", user.getUsername(), batch);
        return batchRepository.save(batch);
    }

    private void assignBatchToUser(Batch batch, UserInfo user) {

        batch.setAssignedUserId(user.getUserId());
        batch.setAssignedUserName(user.getUsername());
        batch.setAssignedAt(LocalDateTime.now());
        batch.setStatus(LifeCycleStatus.IN_PROGRESS);
    }

    private void validateGrtReport(Product product, String serialNumber) {

        if (!Boolean.TRUE.equals(product.getIsGrtCheckRequired())) {
            return;
        }

        GrtReportDetail report = grtReportRepository.findBySerialNo(serialNumber)
                .orElseThrow(() -> {
                    LOG.error("Grt report not found for serial number: {}", serialNumber);
                    return new GrtValidationException("GRT report not found for serial number: " + serialNumber);
                });

        if (!"OK".equalsIgnoreCase(report.getStatus())) {
            LOG.error("GRT report status {} for serial number {}", report.getStatus(), serialNumber);
            throw new GrtValidationException("Product cannot be scanned because GRT status is NG.");
        }
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
    public BatchQrResponseDto reprintBatchQr(String batchSerialNumber) {
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

    @Override
    public Page<BatchSummaryResponseDto> getBatchesByStatus(LifeCycleStatus status, int page, int size) {
        LOG.info("Getting batches by status: {} and page {}, size {}", status, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Batch> batchPage = batchRepository.findByStatus(status, pageable);
        LOG.info("Found {} batches with status: {}", batchPage.getTotalElements(), status);
        return batchPage.map(batch -> {

            Product product = productRepository.findById(batch.getProductId())
                    .orElse(null);

            return new BatchSummaryResponseDto(
                    batch.getBatchSerialNumber(),
                    batch.getCurrentUnits(),
                    batch.getMaxUnits(),
                    batch.getStatus(),
                    product != null ? product.getProductCode() : null,
                    product != null ? product.getName() : null,
                    batch.getProductSerialList(),
                    batch.getCreatedAt(),
                    batch.getClosedAt()
            );
        });
    }

    @Override
    public BatchProductsResponseDto getProductsInBatch(String batchSerialNumber) {

        Batch batch = batchRepository.findByBatchSerialNumber(batchSerialNumber)
                .orElseThrow(() -> new ProductNotFoundException("Batch not found"));

        Product product = productRepository.findById(batch.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        return new BatchProductsResponseDto(
                batch.getBatchSerialNumber(),
                product.getProductCode(),
                batch.getCurrentUnits(),
                batch.getMaxUnits(),
                batch.getProductSerialList()
        );
    }

    @Override
    public List<BatchSummaryDto> getActiveBatches(String productCode) {

        Product product = validateProduct(productCode);

        String username = SecurityUtils.getCurrentUsername();

        List<Batch> batches = batchRepository.findByProductIdAndStatusInOrderByCreatedAtAsc(
                product.getProductId(), List.of(LifeCycleStatus.IN_PROGRESS, LifeCycleStatus.AVAILABLE));
        LOG.info("Found {} batches with status: {}", batches.size(), product.getProductCode());
        return batches.stream().map(batch -> mapToBatchSummary(
                        batch,
                        product.getProductCode(),
                        username))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BatchSummaryDto assignBatch(String batchSerialNumber) {
        LOG.info("Assigning batch: {}", batchSerialNumber);
        UserInfo user = getLoggedInUser();

        Batch batch = batchRepository.findByBatchSerialNumber(batchSerialNumber)
                .orElseThrow(() -> new BatchException("Batch not found"));

        // Validate batch status
        if (batch.getStatus() != LifeCycleStatus.AVAILABLE) {
            throw new BatchException("Batch is no longer available.");
        }

        // Check if user already has an active batch for this product
        Optional<Batch> existingBatch = batchRepository.findByProductIdAndAssignedUserIdAndStatus(
                batch.getProductId(),
                user.getUserId(),
                LifeCycleStatus.IN_PROGRESS);

        if (existingBatch.isPresent()) {
            LOG.error("User {} already has an active batch for product {}: {}", user.getUsername(), batch.getProductId(), existingBatch.get().getBatchSerialNumber());
            throw new BatchException("You already have an active batch for this product." + existingBatch.get().getBatchSerialNumber());
        }

        assignBatchToUser(batch, user);

        Batch savedBatch = batchRepository.save(batch);

        return mapToBatchSummary(
                savedBatch,
                getProduct(savedBatch.getProductId()).getProductCode(),
                user.getUsername());
    }

    @Override
    @Transactional
    public BatchSummaryDto releaseBatch(String batchSerialNumber) {
        LOG.info("Releasing batch: {}", batchSerialNumber);

        UserInfo user = getLoggedInUser();

        Batch batch = batchRepository.findByBatchSerialNumber(batchSerialNumber)
                .orElseThrow(() -> new ProductNotFoundException("Batch not found"));

        if (!user.getUserId().equals(batch.getAssignedUserId())) {
            LOG.error("User {} is trying to release batch {} assigned to user {}", user.getUsername(), batchSerialNumber, batch.getAssignedUserName());
            throw new UnauthorizedBatchAccessException("You are not allowed to release this batch.");
        }

        if (batch.getStatus() != LifeCycleStatus.IN_PROGRESS) {
            LOG.error("User {} is trying to release batch {}", user.getUsername(), batchSerialNumber);
            throw new InvalidBatchStateException("Only an IN_PROGRESS batch can be released.");
        }

        Batch savedBatch = releaseBatchInternal(batch);
        LOG.info("Batch released: {}", savedBatch.getBatchSerialNumber());
        return mapToBatchSummary(
                savedBatch,
                getProduct(savedBatch.getProductId()).getProductCode(),
                user.getUsername());
    }

    @Override
    @Transactional
    public Integer releaseAllActiveBatches(UUID userId) {

        List<Batch> activeBatches = batchRepository.findByAssignedUserIdAndStatus(userId, LifeCycleStatus.IN_PROGRESS);
        activeBatches.forEach(this::releaseBatchInternal);

        LOG.info("Released {} active batches for user {}", activeBatches.size(), userId);
        return activeBatches.size();
    }

    private BatchSummaryDto mapToBatchSummary(Batch batch, String productCode, String loggedInUser) {
        LOG.info("Mapping batch {} to product {}", batch.getBatchSerialNumber(), productCode);
        BatchAction action;

        if (loggedInUser.equals(batch.getAssignedUserName())) {
            action = BatchAction.RESUME;
        } else if (batch.getStatus() == LifeCycleStatus.AVAILABLE) {
            action = BatchAction.CONTINUE;
        } else {
            action = BatchAction.IN_USE;
        }

        return BatchSummaryDto.builder()
                .productCode(productCode)
                .batchSerialNumber(batch.getBatchSerialNumber())
                .currentUnits(batch.getCurrentUnits())
                .maxUnits(batch.getMaxUnits())
                .status(batch.getStatus().name())
                .assignedUser(batch.getAssignedUserName())
                .assignedAt(batch.getAssignedAt())
                .action(action)
                .build();
    }

    private Product getProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
    }

    private UserInfo getLoggedInUser() {
        String username = SecurityUtils.getCurrentUsername();

        return userInfoRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found."));
    }

    private Batch releaseBatchInternal(Batch batch) {

        batch.setAssignedUserId(null);
        batch.setAssignedUserName(null);
        batch.setAssignedAt(null);
        batch.setStatus(LifeCycleStatus.AVAILABLE);

        return batchRepository.save(batch);
    }
}