package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;
@Data
@AllArgsConstructor
public class ProductResponseDto {

    private UUID productId;
    private String productCode;
    private String name;
    private String description;

    private String model;              // ✅ ADD
    private String variant;
    private String manufacturerCode;  // ✅ ADD
    private String partNo;            // ✅ ADD
    private String photoUrl;          // ✅ ADD

    private String sapCode;

    private Integer boxCapacity;
    private Integer palletCapacity;

    private String status;
}