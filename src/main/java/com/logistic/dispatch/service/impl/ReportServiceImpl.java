package com.logistic.dispatch.service.impl;

import com.logistic.dispatch.dto.BatchReportDto;
import com.logistic.dispatch.dto.ProductSummaryDto;
import com.logistic.dispatch.entitiy.Batch;
import com.logistic.dispatch.entitiy.Product;
import com.logistic.dispatch.exception.InvalidDateRangeException;
import com.logistic.dispatch.exception.ProductNotFoundException;
import com.logistic.dispatch.repository.BatchRepository;
import com.logistic.dispatch.repository.PalletRepository;
import com.logistic.dispatch.repository.ProductRepository;
import com.logistic.dispatch.service.ReportService;
import com.logistic.dispatch.utility.LifeCycleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final PalletRepository palletRepository;

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
}