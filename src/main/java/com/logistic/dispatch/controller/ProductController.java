package com.logistic.dispatch.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistic.dispatch.dto.ProductRequestDto;
import com.logistic.dispatch.dto.ProductResponseDto;
import com.logistic.dispatch.exception.RequestValidationException;
import com.logistic.dispatch.service.ProductService;
import com.logistic.dispatch.utility.ProductStatus;
import com.logistic.dispatch.validation.OnCreate;
import com.logistic.dispatch.validation.OnUpdate;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.print.attribute.standard.Media;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    private final ObjectMapper objectMapper;

    private final Validator validator;

    public ProductController(ProductService productService, ObjectMapper objectMapper, Validator validator) {
        this.productService = productService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponseDto> createProduct(@RequestPart("product") String productDetails, @RequestPart MultipartFile productImage) throws Exception {
        ProductRequestDto productRequestDto = objectMapper.readValue(productDetails, ProductRequestDto.class);
        Set<ConstraintViolation<ProductRequestDto>> violations = validator.validate(productRequestDto, OnCreate.class);

        if (!violations.isEmpty()) {
            List<String> errors = violations.stream().map(ConstraintViolation::getMessage).toList();
            throw new RequestValidationException(String.join(", ", errors));
        }
        return ResponseEntity.ok(productService.createProduct(productRequestDto, productImage));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'OPERATOR')")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/id/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'OPERATOR')")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/code/{productCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'OPERATOR')")
    public ResponseEntity<ProductResponseDto> getProductByCode(@PathVariable String productCode) {
        ProductResponseDto response = productService.getProductByCode(productCode);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable UUID id, @Validated(OnUpdate.class) @RequestBody ProductRequestDto dto) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> changeStatus(@PathVariable UUID id, @RequestParam String status) {
        ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(productService.changeProductStatus(id, productStatus));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    @PatchMapping(value = "/{productCode}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponseDto> updateProductImage(@PathVariable String productCode, @RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(productService.updateProductImage(productCode, image));
    }
}

