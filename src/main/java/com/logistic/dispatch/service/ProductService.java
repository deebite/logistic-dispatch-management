package com.logistic.dispatch.service;

import com.logistic.dispatch.dto.ProductRequestDto;
import com.logistic.dispatch.dto.ProductResponseDto;
import com.logistic.dispatch.utility.ProductStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductResponseDto createProduct(ProductRequestDto dto, MultipartFile productImage) throws Exception;

    List<ProductResponseDto> getAllProducts();

    ProductResponseDto getProductById(UUID id);

    ProductResponseDto updateProduct(UUID id, ProductRequestDto dto);

    ProductResponseDto changeProductStatus(UUID id, ProductStatus status);

    void deleteProduct(UUID id);

    ProductResponseDto getProductByCode(String productCode);

    ProductResponseDto updateProductImage(String productCode, MultipartFile image);
}

