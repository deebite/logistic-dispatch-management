package com.logistic.dispatch.service;

import com.logistic.dispatch.dto.*;
import com.logistic.dispatch.utility.LifeCycleStatus;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface BatchService {

    ScanResponseDto scanProduct(ScanProductDto dto);

    QrProcessResponse processPendingQrBatches();

    BulkScanResponseDto bulkScan(BulkScanRequestDto bulkScanRequestDto);

    ManualBatchCloseResponse closeBatch(String batchSerialNumber);

    BatchQrResponseDto reprintBatchQr(String batchSerialNumber);

    Page<BatchSummaryResponseDto> getBatchesByStatus(LifeCycleStatus status, int page, int size);

    BatchProductsResponseDto getProductsInBatch(String batchSerialNumber);

    List<BatchSummaryDto> getActiveBatches(String productCode);

    BatchSummaryDto assignBatch(String batchSerialNumber);

    BatchSummaryDto releaseBatch(String batchSerialNumber);

    Integer releaseAllActiveBatches(UUID userId);
}
