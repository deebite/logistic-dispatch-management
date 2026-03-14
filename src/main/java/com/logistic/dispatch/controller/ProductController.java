package com.logistic.dispatch.controller;

import com.logistic.dispatch.dto.ProductRequestDto;
import com.logistic.dispatch.dto.ProductResponseDto;
import com.logistic.dispatch.service.ProductService;
import com.logistic.dispatch.utility.ProductStatus;
import com.logistic.dispatch.validation.OnCreate;
import com.logistic.dispatch.validation.OnUpdate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    public ResponseEntity<ProductResponseDto> createProduct(@Validated(OnCreate.class) @RequestBody ProductRequestDto dto) {
        return ResponseEntity.ok(productService.createProduct(dto));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/code/{productCode}")
    public ResponseEntity<ProductResponseDto> getProductByCode(@PathVariable String productCode) {
        ProductResponseDto response = productService.getProductByCode(productCode);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable UUID id, @Validated(OnUpdate.class) @RequestBody ProductRequestDto dto) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductResponseDto> changeStatus(@PathVariable UUID id, @RequestParam String status) {
        ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(productService.changeProductStatus(id, productStatus));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }
}

