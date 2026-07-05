package com.logistic.dispatch.service.impl;

import com.logistic.dispatch.dto.*;
import com.logistic.dispatch.entitiy.Batch;
import com.logistic.dispatch.entitiy.Pallet;
import com.logistic.dispatch.entitiy.Product;
import com.logistic.dispatch.exception.ProductNotFoundException;
import com.logistic.dispatch.repository.BatchRepository;
import com.logistic.dispatch.repository.PalletRepository;
import com.logistic.dispatch.repository.ProductRepository;
import com.logistic.dispatch.service.ReportService;
import com.logistic.dispatch.utility.LifeCycleStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final PalletRepository palletRepository;

    private static final Logger LOG = LoggerFactory.getLogger(ReportServiceImpl.class);

    @Override
    public ProductSummaryDto getProductSummary(String productCode, LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now();
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From date cannot be after To date");
        }

        if (to.isAfter(today)) {
            throw new IllegalArgumentException("To date cannot be in the future");
        }

        if (from.isAfter(today)) {
            throw new IllegalArgumentException("From date cannot be in the future");
        }

        // 1️⃣ Validate Product
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        UUID productId = product.getProductId();

        // 2️⃣ Convert date range
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        // 3️⃣ Closed Data (Production)
        Long totalClosedBatches = batchRepository.countClosedBatchesByDate(productId, start, end);

        Long totalClosedPallets = palletRepository.countClosedPalletsByDate(productId, start, end);

        Long totalSerials = batchRepository.sumClosedSerialsByDate(productId, start, end);

        // 4️⃣ Real-time Open Status
        Long openBatches = batchRepository.countByProductIdAndStatus(productId, LifeCycleStatus.OPEN);

        Long openPallets = palletRepository.countByProductIdAndStatus(productId, LifeCycleStatus.OPEN);

        return new ProductSummaryDto(productCode,
                totalClosedBatches != null ? totalClosedBatches : 0,
                totalClosedPallets != null ? totalClosedPallets : 0,
                totalSerials != null ? totalSerials : 0,
                openBatches, openPallets);
    }

    @Override
    public List<BatchReportDto> getBatchReport(String productCode, LocalDate from, LocalDate to) {

        // 1️⃣ Validate Product
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        // 2️⃣ Validate Date Range
        LocalDate today = LocalDate.now();

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From date cannot be after To date");
        }

        if (to.isAfter(today)) {
            throw new IllegalArgumentException("To date cannot be in the future");
        }

        // 3️⃣ Convert to DateTime
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        // 4️⃣ Fetch Data
        List<Batch> batches = batchRepository.findClosedBatchesByDate(product.getProductId(), start, end);

        // 5️⃣ Map to DTO
        return batches.stream().map(batch -> new BatchReportDto(
                batch.getBatchSerialNumber(),
                productCode,
                batch.getCurrentUnits(),
                batch.getMaxUnits(),
                batch.getStatus().name(),
                batch.getQrStatus() != null ? batch.getQrStatus().name() : null,
                batch.getClosedAt())).toList();
    }

    @Override
    public DispatchSummaryResponseDto getDispatchSummary(String productCode,
            LocalDate startDate,
            LocalDate endDate) {

        LOG.info("Getting dispatch summary for product: {}, startDate: {}, endDate: {}",
                productCode, startDate, endDate);

        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        // Convert to LocalDateTime
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        // Fetch Product
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found: " + productCode));

        UUID productId = product.getProductId();

        // Product Summary
        ProductSummary productSummary = ProductSummary.builder()
                .productCode(product.getProductCode())
                .productName(product.getName())
                .manufacturerCode(product.getManufacturerCode())
                .build();

        // Fetch Batches & Pallets for selected date range
        List<Batch> batches = batchRepository.findByProductIdAndCreatedAtBetween(
                productId,
                start,
                end);

        List<Pallet> pallets = palletRepository.findByProductIdAndCreatedAtBetween(
                productId,
                start,
                end);

        // Summary Counts
        Long closedBatches =
                batchRepository.countClosedBatchesByDate(productId, start, end);

        Long inProgressBatches =
                batchRepository.countInProgressBatches(productId, start, end);

        Long closedPallets =
                palletRepository.countClosedPalletsByDate(productId, start, end);

        Long openPallets =
                palletRepository.countOpenPalletsByDate(productId, start, end);

        BatchSummary batchSummary = BatchSummary.builder()
                .total(closedBatches + inProgressBatches)
                .closed(closedBatches)
                .inProgress(inProgressBatches)
                .build();

        PalletSummary palletSummary = PalletSummary.builder()
                .total(closedPallets + openPallets)
                .closed(closedPallets)
                .inProgress(openPallets)
                .build();

        // Batch Serial -> Pallet Map
        Map<String, Pallet> palletMap = new HashMap<>();

        for (Pallet pallet : pallets) {

            if (pallet.getBatchSerialList() != null) {

                for (String batchSerial : pallet.getBatchSerialList()) {
                    palletMap.put(batchSerial, pallet);
                }
            }
        }

        // Details
        List<DispatchDetailDto> details = new ArrayList<>();

        for (Batch batch : batches) {

            Pallet matchedPallet =
                    palletMap.get(batch.getBatchSerialNumber());

            PalletInfoDto palletInfo = null;

            if (matchedPallet != null) {

                palletInfo = PalletInfoDto.builder()
                        .palletSerialNumber(
                                matchedPallet.getPalletSerialNumber())
                        .palletStatus(
                                matchedPallet.getStatus().name())
                        .currentBatches(
                                matchedPallet.getCurrentBatches())
                        .maxBatches(
                                matchedPallet.getMaxBatches())
                        .build();
            }

            DispatchDetailDto detail = DispatchDetailDto.builder()
                    .batchSerialNumber(batch.getBatchSerialNumber())
                    .batchStatus(batch.getStatus().name())
                    .currentUnits(batch.getCurrentUnits())
                    .maxUnits(batch.getMaxUnits())
                    .assignedUser(batch.getAssignedUserName())
                    .createdAt(batch.getCreatedAt())
                    .closedAt(batch.getClosedAt())
                    .productSerialNumbers(batch.getProductSerialList())
                    .pallet(palletInfo)
                    .build();

            details.add(detail);
        }

        // Sort latest first
        details.sort(
                Comparator.comparing(
                        DispatchDetailDto::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder()))
        );

        return DispatchSummaryResponseDto.builder()
                .product(productSummary)
                .batchSummary(batchSummary)
                .palletSummary(palletSummary)
                .details(details)
                .build();
    }
}