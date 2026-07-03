package com.logistic.dispatch.controller;

import com.logistic.dispatch.dto.ManualPalletCloseResponse;
import com.logistic.dispatch.dto.PalletBatchesResponseDto;
import com.logistic.dispatch.dto.PalletQrResponseDto;
import com.logistic.dispatch.dto.PalletSummaryResponseDto;
import com.logistic.dispatch.entitiy.Pallet;
import com.logistic.dispatch.service.PalletService;
import com.logistic.dispatch.utility.LifeCycleStatus;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pallet")
public class PalletController {

    private final PalletService palletService;

    public PalletController(PalletService palletService) {
        this.palletService = palletService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pallet> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(palletService.getPalletById(id));
    }

    @GetMapping("/product/{productCode}")
    public ResponseEntity<Pallet> getOpenByProduct(@PathVariable String productCode) {
        return ResponseEntity.ok(palletService.getOpenPalletByProductCode(productCode));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Pallet>> getByStatus(@PathVariable LifeCycleStatus status) {
        return ResponseEntity.ok(palletService.getPalletsByStatus(status));
    }

    @PostMapping("/{palletSerialNumber}/close")
    public ResponseEntity<ManualPalletCloseResponse> closePallet(@PathVariable String palletSerialNumber) {
        return ResponseEntity.ok(palletService.closePalletManually(palletSerialNumber));
    }

    @GetMapping("/{palletSerialNumber}/reprint-qr")
    public ResponseEntity<PalletQrResponseDto> reprintPalletQr(@PathVariable String palletSerialNumber) {
        return ResponseEntity.ok(palletService.reprintPalletQr(palletSerialNumber));
    }

    @GetMapping
    public ResponseEntity<Page<PalletSummaryResponseDto>> getPalletsByStatus(@RequestParam LifeCycleStatus status,
                                                                             @RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(palletService.getPalletsByStatus(status, page, size));
    }

    @GetMapping("/{palletSerialNumber}/batches")
    public ResponseEntity<PalletBatchesResponseDto>
    getBatchesInPallet(@PathVariable String palletSerialNumber) {
        return ResponseEntity.ok(palletService.getBatchesInPallet(palletSerialNumber));
    }
}
