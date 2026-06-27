package com.logistic.dispatch.repository;

import com.logistic.dispatch.entitiy.GrtReportDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrtReportRepository extends JpaRepository<GrtReportDetail, Long> {

    Optional<GrtReportDetail> findBySerialNo(String serialNo);
}
