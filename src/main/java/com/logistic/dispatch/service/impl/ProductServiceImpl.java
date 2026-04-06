package com.logistic.dispatch.service.impl;

import com.logistic.dispatch.dto.ProductRequestDto;
import com.logistic.dispatch.dto.ProductResponseDto;
import com.logistic.dispatch.entitiy.Product;
import com.logistic.dispatch.exception.ProductAlreadyExistsException;
import com.logistic.dispatch.exception.ProductNotFoundException;
import com.logistic.dispatch.mapper.ProductMapper;
import com.logistic.dispatch.repository.ProductRepository;
import com.logistic.dispatch.service.ProductService;
import com.logistic.dispatch.utility.ProductStatus;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductResponseDto createProduct(ProductRequestDto dto) {

        if (productRepository.existsByProductCode(dto.getProductCode())) {
            throw new ProductAlreadyExistsException("Product code already exists");
        }

        Product product = ProductMapper.toEntity(dto);

        Product saved = productRepository.save(product);

        return ProductMapper.toResponse(saved);
    }

    @Override
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll().stream().map(ProductMapper::toResponse).toList();
    }

    @Override
    public ProductResponseDto getProductById(UUID id) {

        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found"));
        return ProductMapper.toResponse(product);
    }

    @Override
    public ProductResponseDto updateProduct(UUID id, ProductRequestDto dto) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        // 🔥 Product Code (check duplicate only if changed)
        if (dto.getProductCode() != null &&
                !dto.getProductCode().equals(product.getProductCode())) {

            if (productRepository.existsByProductCode(dto.getProductCode())) {
                throw new ProductAlreadyExistsException("Product code already exists");
            }

            product.setProductCode(dto.getProductCode());
        }

        // 🔥 Safe field updates
        if (dto.getName() != null) {
            product.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }

        if (dto.getModel() != null) {
            product.setModel(dto.getModel());
        }

        if (dto.getVariant() != null) {
            product.setVariant(dto.getVariant());
        }

        if (dto.getManufacturerCode() != null) {
            product.setManufacturerCode(dto.getManufacturerCode());
        }

        if (dto.getPartNo() != null) {
            product.setPartNo(dto.getPartNo());
        }

        if (dto.getPhotoUrl() != null) {
            product.setPhotoUrl(dto.getPhotoUrl());
        }

        if (dto.getBoxCapacity() != null) {
            product.setBoxCapacity(dto.getBoxCapacity());
        }

        if (dto.getPalletCapacity() != null) {
            product.setPalletCapacity(dto.getPalletCapacity());
        }

        if (dto.getSapCode() != null) {
            product.setSapCode(dto.getSapCode());
        }

        Product updatedProduct = productRepository.save(product);

        return ProductMapper.toResponse(updatedProduct);
    }


    @Override
    public ProductResponseDto changeProductStatus(UUID id, ProductStatus status) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        product.setStatus(status);

        return ProductMapper.toResponse(productRepository.save(product));
    }


    @Override
    public void deleteProduct(UUID id) {

        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found"));

        productRepository.delete(product);
    }

    @Override
    public ProductResponseDto getProductByCode(String productCode) {
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        return new ProductResponseDto(product.getProductId(), product.getProductCode(), product.getName(),product.getDescription(), product.getSapCode(), product.getVariant(), product.getBoxCapacity(), product.getPalletCapacity(), product.getStatus().toString());
    }

}
