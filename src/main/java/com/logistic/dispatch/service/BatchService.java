package com.logistic.dispatch.service;

import com.logistic.dispatch.dto.*;

public interface BatchService {

    ScanResponseDto scanProduct(ScanProductDto dto);

    QrProcessResponse processPendingQrBatches();

    BulkScanResponseDto bulkScan(BulkScanRequestDto bulkScanRequestDto);
}
