package com.logistic.dispatch.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistic.dispatch.dto.ManualPalletCloseResponse;
import com.logistic.dispatch.dto.PalletBatchesResponseDto;
import com.logistic.dispatch.dto.PalletQrResponseDto;
import com.logistic.dispatch.dto.PalletSummaryResponseDto;
import com.logistic.dispatch.entitiy.Batch;
import com.logistic.dispatch.entitiy.Pallet;
import com.logistic.dispatch.entitiy.Product;
import com.logistic.dispatch.exception.AlreadyClosed;
import com.logistic.dispatch.exception.PalletAssignmentException;
import com.logistic.dispatch.exception.ProductNotFoundException;
import com.logistic.dispatch.exception.UserNotFoundException;
import com.logistic.dispatch.repository.PalletRepository;
import com.logistic.dispatch.repository.ProductRepository;
import com.logistic.dispatch.service.PalletService;
import com.logistic.dispatch.utility.LifeCycleStatus;
import com.logistic.dispatch.utility.QrService;
import com.logistic.dispatch.utility.QrStatus;
import jakarta.persistence.OptimisticLockException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PalletServiceImpl implements PalletService {

    private final PalletRepository palletRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final QrService qrService;
    private static final Logger LOG = LoggerFactory.getLogger(PalletServiceImpl.class);

    public PalletServiceImpl(PalletRepository palletRepository, ProductRepository productRepository, ObjectMapper objectMapper, QrService qrService) {
        this.palletRepository = palletRepository;
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
        this.qrService = qrService;
    }

    @Override
    public Pallet assignBatchToPallet(Batch batch) {
        LOG.info("Coming batch: {}", batch);
        Product product = productRepository.findById(batch.getProductId())
                .orElseThrow(() -> new PalletAssignmentException("Product not found for pallet assignment"));

        Pallet pallet = getOrCreateOpenPallet(product);

        List<String> batchList = pallet.getBatchSerialList();

        if (batchList.contains(batch.getBatchSerialNumber())) {
            LOG.info("Batch already assigned to pallet: {}", batch);
            throw new PalletAssignmentException("Batch already assigned to pallet: " + pallet.getPalletSerialNumber());
        }

        batchList.add(batch.getBatchSerialNumber());

        pallet.setCurrentBatches(batchList.size());
        pallet.setBatchSerialList(batchList);

        if (pallet.getCurrentBatches().equals(pallet.getMaxBatches())) {

            pallet.setStatus(LifeCycleStatus.CLOSED);
            pallet.setClosedAt(LocalDateTime.now());

            qrService.generatePalletQr(pallet, batchList);
        }

        try {
            Pallet savedPallet = palletRepository.save(pallet);
            LOG.info("Saving batch in pallet: {}", savedPallet);
            return savedPallet;
        } catch (OptimisticLockException e) {
            LOG.error("Concurrent update detected while assigning batch to pallet: {}", e.getMessage());
            throw new PalletAssignmentException("Concurrent update detected while assigning batch to pallet");
        }
    }

    @Override
    public Pallet getPalletById(UUID palletId) {
        LOG.info("Getting pallet_id: {}", palletId);
        return palletRepository.findById(palletId)
                .orElseThrow(() -> new PalletAssignmentException("Pallet not found with ID: " + palletId));
    }

    @Override
    public Pallet getOpenPalletByProductCode(String productCode) {
        LOG.info("Getting open pallet for product code: {}", productCode);
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new PalletAssignmentException("Product not found: " + productCode));

        List<Pallet> openPallets = palletRepository.findByProductIdAndStatus(product.getProductId(), LifeCycleStatus.OPEN);

        if (openPallets.isEmpty()) {
            LOG.error("No open pallets found for product code: {}", productCode);
            throw new PalletAssignmentException("No OPEN pallet found for product: " + productCode);
        }

        if (openPallets.size() > 1) {
            LOG.error("Multiple open pallets found for product code: {}", productCode);
            throw new PalletAssignmentException("Data integrity error: Multiple OPEN pallets found for product: " + productCode);
        }
        return openPallets.get(0);
    }

    @Override
    public List<Pallet> getPalletsByStatus(LifeCycleStatus status) {
        LOG.info("Getting pallets by status: {}", status);
        List<Pallet> pallets = palletRepository.findByStatus(status);

        if (pallets.isEmpty()) {
            LOG.error("No pallets found for status: {}", status);
            throw new PalletAssignmentException("No pallets found with status: " + status);
        }

        return pallets;
    }

    @Override
    public ManualPalletCloseResponse closePalletManually(String palletSerialNumber) {
        LOG.info("Closing pallet manually: {}", palletSerialNumber);
        Pallet pallet = palletRepository.findByPalletSerialNumber(palletSerialNumber)
                .orElseThrow(() -> new UserNotFoundException("Pallet not found!"));

        if (pallet.getStatus() == LifeCycleStatus.CLOSED) {
            LOG.error("Pallet is already closed: {}", palletSerialNumber);
            throw new AlreadyClosed("Pallet already closed!");
        }

        pallet.setStatus(LifeCycleStatus.CLOSED);
        pallet.setClosedAt(LocalDateTime.now());
        pallet.setQrStatus(QrStatus.PENDING);

        qrService.generatePalletQr(pallet, pallet.getBatchSerialList());

        palletRepository.save(pallet);

        String qrImage = qrService.getQrImageBase64(pallet.getQrCodePath());
        LOG.info("QR image generated: {}", qrImage);
        return new ManualPalletCloseResponse("Pallet closed successfully",
                pallet.getPalletSerialNumber(), qrImage);
    }

    private Pallet getOrCreateOpenPallet(Product product) {
        LOG.info("Getting or creating open pallet for product: {}", product.getProductCode());
        List<Pallet> openPallets = palletRepository.findByProductIdAndStatus(product.getProductId(), LifeCycleStatus.OPEN);

        if (openPallets.size() > 1) {
            LOG.error("Data integrity error: Multiple OPEN pallets found for product: {}", product.getProductCode());
            throw new PalletAssignmentException("Data integrity error: Multiple OPEN pallets found for product: " + product.getProductCode());
        }

        if (openPallets.isEmpty()) {
            LOG.info("No open pallet found for product: {}", product.getProductCode());
            return createNewPallet(product);
        }
        return openPallets.get(0);
    }

    private Pallet createNewPallet(Product product) {
        LOG.info("Creating new pallet: {}", product.getProductCode());
        LocalDate today = LocalDate.now();
        String formattedDate = today.format(DateTimeFormatter.BASIC_ISO_DATE);

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        long todayCount = palletRepository.countByProductIdAndCreatedAtBetween(product.getProductId(), startOfDay, endOfDay);

        String sequenceFormatted = String.format("%04d", todayCount + 1);

        String palletNumber = "P-" + product.getProductCode() + "-" + formattedDate + "-" + sequenceFormatted;

        Pallet pallet = new Pallet();
        pallet.setPalletSerialNumber(palletNumber);
        pallet.setProductId(product.getProductId());
        pallet.setMaxBatches(product.getPalletCapacity());
        pallet.setCurrentBatches(0);
        pallet.setStatus(LifeCycleStatus.OPEN);
        pallet.setBatchSerialList(new ArrayList<>());
        LOG.info("New pallet created: {}", pallet);
        return palletRepository.save(pallet);
    }

    @Override
    public PalletQrResponseDto reprintPalletQr(String palletSerialNumber) {
        LOG.info("Reprinting QR for pallet: {}", palletSerialNumber);
        Pallet pallet = palletRepository.findByPalletSerialNumber(palletSerialNumber)
                .orElseThrow(() -> new UserNotFoundException("Pallet not found!"));

        if (pallet.getStatus() != LifeCycleStatus.CLOSED) {
            LOG.error("QR can only be reprinted for CLOSED pallet: {}", palletSerialNumber);
            throw new RuntimeException("QR can only be reprinted for CLOSED pallet");
        }

        String qrImage = qrService.generatePalletQrBase64(pallet);
        LOG.info("Reprinting QR for pallet: {}", palletSerialNumber);
        return new PalletQrResponseDto(pallet.getPalletSerialNumber(), qrImage);
    }

    @Override
    public Page<PalletSummaryResponseDto> getPalletsByStatus(LifeCycleStatus status, int page, int size) {
        LOG.info("Getting pallets by status: {}, page: {}, size: {}", status, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Pallet> palletPage = palletRepository.findByStatus(status, pageable);
        LOG.info("Found {} pallets with status: {}", palletPage.getTotalElements(), status);
        return palletPage.map(pallet -> {

            Product product = productRepository.findById(pallet.getProductId())
                    .orElse(null);

            return new PalletSummaryResponseDto(
                    pallet.getPalletSerialNumber(),
                    pallet.getCurrentBatches(),
                    pallet.getMaxBatches(),
                    pallet.getStatus(),
                    product != null ? product.getProductCode() : null,
                    product != null ? product.getName() : null,
                    pallet.getBatchSerialList(),
                    pallet.getCreatedAt(),
                    pallet.getClosedAt()
            );
        });
    }


    @Override
    public PalletBatchesResponseDto getBatchesInPallet(String palletSerialNumber) {
        LOG.info("Getting batches in pallet: {}", palletSerialNumber);
        Pallet pallet = palletRepository.findByPalletSerialNumber(palletSerialNumber)
                .orElseThrow(() -> new ProductNotFoundException("Pallet not found"));

        Product product = productRepository.findById(pallet.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        return new PalletBatchesResponseDto(
                pallet.getPalletSerialNumber(),
                product.getProductCode(),
                pallet.getCurrentBatches(),
                pallet.getMaxBatches(),
                pallet.getBatchSerialList()
        );
    }
}