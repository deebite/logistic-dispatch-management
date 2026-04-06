package com.logistic.dispatch.mapper;

import com.logistic.dispatch.dto.ProductRequestDto;
import com.logistic.dispatch.dto.ProductResponseDto;
import com.logistic.dispatch.entitiy.Product;
import com.logistic.dispatch.utility.ProductStatus;

public class ProductMapper {

    public static Product toEntity(ProductRequestDto dto) {

        Product product = new Product();

        product.setProductCode(dto.getProductCode());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setModel(dto.getModel());
        product.setVariant(dto.getVariant());
        product.setManufacturerCode(dto.getManufacturerCode());
        product.setPartNo(dto.getPartNo());
        product.setPhotoUrl(dto.getPhotoUrl());
        product.setBoxCapacity(dto.getBoxCapacity());
        product.setPalletCapacity(dto.getPalletCapacity());
        product.setSapCode(dto.getSapCode());
        product.setStatus(ProductStatus.ACTIVE);

        return product;
    }

    // Convert Entity → ResponseDTO
    public static ProductResponseDto toResponse(Product product) {

        return new ProductResponseDto (
                product.getProductId(),
                product.getProductCode(),
                product.getName(),
                product.getDescription(),
                product.getSapCode(),
                product.getVariant(),
                product.getBoxCapacity(),
                product.getPalletCapacity(),
                product.getStatus().name()
        );
    }
}
