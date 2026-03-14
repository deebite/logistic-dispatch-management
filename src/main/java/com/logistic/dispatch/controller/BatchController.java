package com.logistic.dispatch.controller;

import com.logistic.dispatch.dto.*;
import com.logistic.dispatch.service.BatchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batch")
@PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
public class BatchController {

    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @PostMapping("/scan")
    public ResponseEntity<ScanResponseDto> scanProduct(@Valid @RequestBody ScanProductDto dto) {
        ScanResponseDto response = batchService.scanProduct(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk-scan")
    public ResponseEntity<BulkScanResponseDto> bulkScan(@RequestBody BulkScanRequestDto dto) {
        BulkScanResponseDto response = batchService.bulkScan(dto);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/process-pending-qrs")
    public ResponseEntity<QrProcessResponse> processPendingQr() {
        QrProcessResponse response = batchService.processPendingQrBatches();
        return ResponseEntity.ok(response);
    }
}
