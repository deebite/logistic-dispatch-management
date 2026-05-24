package com.logistic.dispatch.utility;

import com.logistic.dispatch.exception.ImageUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Component
public class ImageUtility {

    @Value("${product.image.path}")
    private String imageUploadPath;

    private static final Logger LOG = LoggerFactory.getLogger(ImageUtility.class);

    public String saveOrUpdateProductImage(String oldImagePath, MultipartFile image) {

        LOG.info("Saving product image to file");

        try {

            if (image == null || image.isEmpty()) {
                throw new ImageUploadException("Image file is required");
            }

            String contentType = image.getContentType();

            if (!List.of("image/png", "image/jpeg", "image/jpg").contains(contentType)) {
                throw new ImageUploadException("Only PNG and JPG images are allowed");
            }

            if (oldImagePath != null && !oldImagePath.isBlank()) {
                Files.deleteIfExists(Paths.get(oldImagePath));
            }

            // ✅ Create upload folder
            Path uploadPath = Paths.get(imageUploadPath);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            LOG.info("Product image path created successfully: {}", filePath);
            return filePath.toString();

        } catch (IOException e) {
            LOG.error("Failed to upload/update product image", e);
            throw new ImageUploadException("Failed to upload product image");
        }
    }
}
