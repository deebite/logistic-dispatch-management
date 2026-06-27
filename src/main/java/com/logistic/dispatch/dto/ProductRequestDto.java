package com.logistic.dispatch.dto;

import com.logistic.dispatch.validation.OnCreate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @NotNull(message = "Monthly Target is required", groups = OnCreate.class)
    @Positive(message = "Monthly target must be positive", groups = OnCreate.class)
    private Integer monthlyTarget;

//    @NotBlank(message = "Photo URL is required", groups = OnCreate.class)
//    private String photoUrl;

    @NotNull(message = "Box capacity is required", groups = OnCreate.class)
    @Positive(message = "Box capacity must be positive", groups = OnCreate.class)
    private Integer boxCapacity;

    @NotNull(message = "Pallet capacity is required", groups = OnCreate.class)
    @Positive(message = "Pallet capacity must be positive", groups = OnCreate.class)
    private Integer palletCapacity;

    @NotBlank(message = "SAP Code is required", groups = OnCreate.class)
    private String sapCode;

    @NotBlank(message = "Revision is required", groups = OnCreate.class)
    private String revisionCode;

    @NotNull(message = "Is GRT status is required?", groups = OnCreate.class)
    private Boolean grtCheckRequired;
}
