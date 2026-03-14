package com.logistic.dispatch.dto;

import com.logistic.dispatch.validation.OnCreate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductRequestDto {

    @NotBlank(message = "Product code is required", groups = OnCreate.class)
    private String productCode;

    @NotBlank(message = "Name is required", groups = OnCreate.class)
    private String name;

    @NotBlank(message = "Description is required", groups = OnCreate.class)
    private String description;

    @NotBlank(message = "Model is required", groups = OnCreate.class)
    private String model;

    @NotBlank(message = "Variant is required", groups = OnCreate.class)
    private String variant;

    @NotBlank(message = "Manufacture Code is required", groups = OnCreate.class)
    private String manufacturerCode;

    @NotBlank(message = "Part No is required", groups = OnCreate.class)
    private String partNo;

    @NotBlank(message = "Photo URL is required", groups = OnCreate.class)
    private String photoUrl;

    @NotNull
    @Min(value = 1, message = "Box capacity must be at least 1", groups = OnCreate.class)
    private Integer boxCapacity;

    @NotNull
    @Min(value = 1, message = "Pallet capacity must be at least 1", groups = OnCreate.class)
    private Integer palletCapacity;

    @NotBlank(message = "SAP Code is required", groups = OnCreate.class)
    private String sapCode;
}
