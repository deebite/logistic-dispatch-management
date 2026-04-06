package com.logistic.dispatch.service;

import com.logistic.dispatch.dto.ManualPalletCloseResponse;
import com.logistic.dispatch.entitiy.Batch;
import com.logistic.dispatch.entitiy.Pallet;
import com.logistic.dispatch.utility.LifeCycleStatus;

import java.util.List;
import java.util.UUID;

public interface PalletService {

    void assignBatchToPallet(Batch batch);

    Pallet getPalletById(UUID palletId);

    Pallet getOpenPalletByProductCode(String productCode);

    List<Pallet> getPalletsByStatus(LifeCycleStatus status);

    ManualPalletCloseResponse closePalletManually(String palletSerialNumber);
}
