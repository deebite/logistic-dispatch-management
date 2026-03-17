package com.logistic.dispatch.entitiy;

import com.logistic.dispatch.utility.LifeCycleStatus;
import com.logistic.dispatch.utility.QrStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "batch")
public class Batch extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID batchId;

    @Column(name = "batch_serial_number", unique = true, nullable = false)
    private String batchSerialNumber;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "max_units", nullable = false)
    private Integer maxUnits;

    @Column(name = "current_units", nullable = false)
    private Integer currentUnits = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "product_serial_list")
    private List<String> productSerialList;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LifeCycleStatus status;

    private String qrCodePath;

    @Enumerated(EnumType.STRING)
    private QrStatus qrStatus;

    private LocalDateTime qrGeneratedAt;

    private LocalDateTime closedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Version
    @Column(name = "version")
    private Long version;
}