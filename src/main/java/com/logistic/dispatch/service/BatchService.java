package com.logistic.dispatch.service;

import com.logistic.dispatch.dto.*;
import com.logistic.dispatch.entitiy.Batch;

public interface BatchService {

    ScanResponseDto scanProduct(ScanProductDto dto);

    QrProcessResponse processPendingQrBatches();

    BulkScanResponseDto bulkScan(BulkScanRequestDto bulkScanRequestDto);

    ManualBatchCloseResponse closeBatchManually(String batchSerialNumber);
}
