package com.logistic.dispatch.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistic.dispatch.entitiy.Batch;
import com.logistic.dispatch.entitiy.Pallet;
import com.logistic.dispatch.entitiy.Product;
import com.logistic.dispatch.exception.JsonProcessingCustomException;
import com.logistic.dispatch.exception.PalletAssignmentException;
import com.logistic.dispatch.repository.PalletRepository;
import com.logistic.dispatch.repository.ProductRepository;
import com.logistic.dispatch.service.PalletService;
import com.logistic.dispatch.utility.LifeCycleStatus;
import com.logistic.dispatch.utility.QrService;
import jakarta.persistence.OptimisticLockException;
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

    public PalletServiceImpl(PalletRepository palletRepository, ProductRepository productRepository, ObjectMapper objectMapper, QrService qrService) {
        this.palletRepository = palletRepository;
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
        this.qrService = qrService;
    }

    @Override
    public void assignBatchToPallet(Batch batch) {

        Product product = productRepository.findById(batch.getProductId())
                .orElseThrow(() -> new PalletAssignmentException("Product not found for pallet assignment"));

        Pallet pallet = getOrCreateOpenPallet(product);

        List<String> batchList = pallet.getBatchSerialList() ;

        if (batchList.contains(batch.getBatchSerialNumber())) {
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
            palletRepository.save(pallet);
        } catch (OptimisticLockException e) {
            throw new PalletAssignmentException("Concurrent update detected while assigning batch to pallet");
        }
    }

    @Override
    public Pallet getPalletById(UUID palletId) {
        return palletRepository.findById(palletId)
                .orElseThrow(() -> new PalletAssignmentException("Pallet not found with ID: " + palletId));
    }

    @Override
    public Pallet getOpenPalletByProductCode(String productCode) {

        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new PalletAssignmentException("Product not found: " + productCode));

        List<Pallet> openPallets = palletRepository.findByProductIdAndStatus(product.getProductId(), LifeCycleStatus.OPEN);

        if (openPallets.isEmpty()) {
            throw new PalletAssignmentException("No OPEN pallet found for product: " + productCode);
        }

        if (openPallets.size() > 1) {
            throw new PalletAssignmentException("Data integrity error: Multiple OPEN pallets found for product: " + productCode);
        }
        return openPallets.get(0);
    }

    @Override
    public List<Pallet> getPalletsByStatus(LifeCycleStatus status) {

        List<Pallet> pallets = palletRepository.findByStatus(status);

        if (pallets.isEmpty()) {
            throw new PalletAssignmentException("No pallets found with status: " + status);
        }

        return pallets;
    }

    private Pallet getOrCreateOpenPallet(Product product) {

        List<Pallet> openPallets = palletRepository.findByProductIdAndStatus(product.getProductId(), LifeCycleStatus.OPEN);

        if (openPallets.size() > 1) {
            throw new PalletAssignmentException("Data integrity error: Multiple OPEN pallets found for product: " + product.getProductCode());
        }

        if (openPallets.isEmpty()) {
            return createNewPallet(product);
        }
        return openPallets.get(0);
    }

    private Pallet createNewPallet(Product product) {

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

        return palletRepository.save(pallet);
    }

    private List<String> getBatchList(String json) {
        try {
            if (json != null && !json.isBlank()) {
                return objectMapper.readValue(json, new TypeReference<List<String>>() {
                });
            }
            return new ArrayList<>();
        } catch (Exception e) {
            throw new JsonProcessingCustomException("Failed to parse pallet batch list", e);
        }
    }

    private String convertToJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            throw new JsonProcessingCustomException("Failed to serialize pallet batch list", e);
        }
    }
}