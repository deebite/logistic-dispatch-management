package com.logistic.dispatch.service;

import com.logistic.dispatch.dto.*;
import com.logistic.dispatch.utility.LifeCycleStatus;
import org.springframework.data.domain.Page;

public interface BatchService {

    ScanResponseDto scanProduct(ScanProductDto dto);

    QrProcessResponse processPendingQrBatches();

    BulkScanResponseDto bulkScan(BulkScanRequestDto bulkScanRequestDto);

    ManualBatchCloseResponse closeBatchManually(String batchSerialNumber);

    BatchQrResponseDto reprintBatchQr(String batchSerialNumber);

    Page<BatchSummaryResponseDto> getBatchesByStatus(LifeCycleStatus status, int page, int size);
}
