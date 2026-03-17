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
@Table(name = "pallet")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pallet extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID palletId;

    @Column(unique = true, nullable = false)
    private String palletSerialNumber;

    @Column(name = "product_id", nullable = false)
    private UUID productId;   // ✅ REQUIRED

    private Integer maxBatches;

    private Integer currentBatches = 0;   // ✅ safer

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "batch_serial_list")
    private List<String> batchSerialList;

    @Enumerated(EnumType.STRING)
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
    private Long version = 0L;
}

