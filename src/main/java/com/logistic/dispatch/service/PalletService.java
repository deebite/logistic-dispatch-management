package com.logistic.dispatch.service;

import com.logistic.dispatch.dto.ManualPalletCloseResponse;
import com.logistic.dispatch.dto.PalletBatchesResponseDto;
import com.logistic.dispatch.dto.PalletQrResponseDto;
import com.logistic.dispatch.dto.PalletSummaryResponseDto;
import com.logistic.dispatch.entitiy.Batch;
import com.logistic.dispatch.entitiy.Pallet;
import com.logistic.dispatch.utility.LifeCycleStatus;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface PalletService {

    Pallet assignBatchToPallet(Batch batch);

    Pallet getPalletById(UUID palletId);

    Pallet getOpenPalletByProductCode(String productCode);

    List<Pallet> getPalletsByStatus(LifeCycleStatus status);

    ManualPalletCloseResponse closePalletManually(String palletSerialNumber);

    PalletQrResponseDto reprintPalletQr(String palletSerialNumber);

    Page<PalletSummaryResponseDto> getPalletsByStatus(LifeCycleStatus status, int page, int size);

    PalletBatchesResponseDto getBatchesInPallet(String palletSerialNumber);
}
