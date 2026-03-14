package com.logistic.dispatch.service;

import com.logistic.dispatch.dto.ProductRequestDto;
import com.logistic.dispatch.dto.ProductResponseDto;
import com.logistic.dispatch.utility.ProductStatus;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductResponseDto createProduct(ProductRequestDto dto);

    List<ProductResponseDto> getAllProducts();

    ProductResponseDto getProductById(UUID id);

    ProductResponseDto updateProduct(UUID id, ProductRequestDto dto);

    ProductResponseDto changeProductStatus(UUID id, ProductStatus status);

    void deleteProduct(UUID id);

    ProductResponseDto getProductByCode(String productCode);
}

