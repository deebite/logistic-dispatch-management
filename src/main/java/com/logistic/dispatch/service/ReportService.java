package com.logistic.dispatch.service;

import com.logistic.dispatch.dto.BatchReportDto;
import com.logistic.dispatch.dto.ProductSummaryDto;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    ProductSummaryDto getProductSummary(String productCode, LocalDate from, LocalDate to);

    List<BatchReportDto> getBatchReport(String productCode, LocalDate from, LocalDate to);
}
