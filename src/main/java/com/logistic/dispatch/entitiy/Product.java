package com.logistic.dispatch.entitiy;

import com.logistic.dispatch.utility.ProductStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_table")
public class Product extends BaseEntity{

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID productId;

    @Column(name = "product_code", unique = true, nullable = false)
    private String productCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "model")
    private String model;

    @Column(name = "variant")
    private String variant;

    @Column(name = "manufacturer_code")
    private String manufacturerCode;

    @Column(name = "part_no")
    private String partNo;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "box_capacity", nullable = false)
    private Integer boxCapacity;

    @Column(name = "pallet_capacity", nullable = false)
    private Integer palletCapacity;

    @Column(name = "sap_code")
    private String sapCode;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ProductStatus status;
}
