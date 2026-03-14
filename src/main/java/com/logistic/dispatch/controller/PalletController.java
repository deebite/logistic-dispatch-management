package com.logistic.dispatch.controller;

import com.logistic.dispatch.entitiy.Pallet;
import com.logistic.dispatch.service.PalletService;
import com.logistic.dispatch.utility.LifeCycleStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pallet")
@PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
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
}
