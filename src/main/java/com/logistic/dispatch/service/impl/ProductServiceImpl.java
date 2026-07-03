package com.logistic.dispatch.service.impl;

import com.logistic.dispatch.dto.ProductRequestDto;
import com.logistic.dispatch.dto.ProductResponseDto;
import com.logistic.dispatch.entitiy.Product;
import com.logistic.dispatch.exception.ProductAlreadyExistsException;
import com.logistic.dispatch.exception.ProductNotFoundException;
import com.logistic.dispatch.mapper.ProductMapper;
import com.logistic.dispatch.repository.ProductRepository;
import com.logistic.dispatch.service.ProductService;
import com.logistic.dispatch.utility.ImageUtility;
import com.logistic.dispatch.utility.ProductStatus;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;

    private final ImageUtility imageUtility;

    public ProductServiceImpl(ProductRepository productRepository, ImageUtility imageUtility) {
        this.productRepository = productRepository;
        this.imageUtility = imageUtility;
    }

    @Override
    public ProductResponseDto createProduct(ProductRequestDto dto, MultipartFile productImage) {
        LOG.info("Creating product with details {}", dto);
        if (productRepository.existsByProductCode(dto.getProductCode())) {
            throw new ProductAlreadyExistsException("Product code already exists");
        }

        Product product = ProductMapper.toEntity(dto);
        String imagePath = imageUtility.saveOrUpdateProductImage(null, productImage);

        product.setPhotoUrl(imagePath);
        Product saved = productRepository.save(product);
        LOG.info("Saved product {}", saved);
        return ProductMapper.toResponse(saved);
    }

    @Override
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll().stream().map(ProductMapper::toResponse).toList();
    }

    @Override
    public ProductResponseDto getProductById(UUID id) {
        LOG.info("Retrieving product with id {}", id);
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found"));
        LOG.info("Returning product {}", product);
        return ProductMapper.toResponse(product);
    }

    @Override
    public ProductResponseDto updateProduct(UUID id, ProductRequestDto dto) {
        LOG.info("Updating product with id {} and details: {}", id, dto.toString());
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

        if (dto.getMonthlyTarget() != null) {
            product.setMonthlyTarget(dto.getMonthlyTarget());
        }

//        if (dto.getPhotoUrl() != null) {
//            product.setPhotoUrl(dto.getPhotoUrl());
//        }

        if (dto.getBoxCapacity() != null) {
            product.setBoxCapacity(dto.getBoxCapacity());
        }

        if (dto.getPalletCapacity() != null) {
            product.setPalletCapacity(dto.getPalletCapacity());
        }

        if (dto.getSapCode() != null) {
            product.setSapCode(dto.getSapCode());
        }

        if (dto.getRevisionCode() != null) {
            product.setRevisionCode(dto.getRevisionCode());
        }

        if (dto.getStatus() != null) {
            product.setStatus(dto.getStatus());
        }

        Product updatedProduct = productRepository.save(product);
        LOG.info("Updated product {}", updatedProduct);
        return ProductMapper.toResponse(updatedProduct);
    }


    @Override
    public ProductResponseDto changeProductStatus(UUID id, ProductStatus status) {
        LOG.info("Changing status of product with id {} and status: {}", id, status);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        product.setStatus(status);
        LOG.info("Returning product {}", product);
        return ProductMapper.toResponse(productRepository.save(product));
    }


    @Override
    public void deleteProduct(UUID id) {
        LOG.info("Deleting product with id {}", id);
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found"));
        productRepository.delete(product);
    }

    @Override
    public ProductResponseDto getProductByCode(String productCode) {
        LOG.info("Retrieving product with product_code {}", productCode);
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        return new ProductResponseDto(product.getProductId(), product.getProductCode(), product.getName(), product.getDescription(), product.getSapCode(), product.getVariant(), product.getBoxCapacity(), product.getPalletCapacity(), product.getManufacturerCode(), product.getRevisionCode(), product.getModel(), product.getMonthlyTarget(), product.getPhotoUrl(), product.getStatus().toString(), product.getIsGrtCheckRequired());
    }

    @Override
    public ProductResponseDto updateProductImage(String productCode, MultipartFile productImage) {
        LOG.info("Updating product image for product_code: {}", productCode);
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        String imagePath = imageUtility.saveOrUpdateProductImage(product.getPhotoUrl(), productImage);
        product.setPhotoUrl(imagePath);
        Product saved = productRepository.save(product);

        LOG.info("Saved product {}", saved);
        return ProductMapper.toResponse(saved);
    }

}
