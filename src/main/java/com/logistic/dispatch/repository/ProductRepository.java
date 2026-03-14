package com.logistic.dispatch.repository;

import com.logistic.dispatch.entitiy.Product;
import com.logistic.dispatch.utility.LifeCycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByProductCode(String productCode);

    boolean existsByProductCode(String productCode);

    List<Product> findByStatus(LifeCycleStatus status);

    Optional<Product> findByProductId(UUID productId);
}
