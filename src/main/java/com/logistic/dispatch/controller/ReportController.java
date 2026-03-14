package com.logistic.dispatch.controller;

import com.logistic.dispatch.dto.BatchReportDto;
import com.logistic.dispatch.dto.ProductSummaryDto;
import com.logistic.dispatch.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/report")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/product-summary")
    public ResponseEntity<ProductSummaryDto> getProductSummary(@RequestParam String productCode, @RequestParam(required = false) String from, @RequestParam(required = false) String to) {

        DateFormatter dates = getDateFormatter(from, to);

        return ResponseEntity.ok(reportService.getProductSummary(productCode, dates.fromDate(), dates.toDate()));
    }


    @GetMapping("/batch-report")
    public ResponseEntity<List<BatchReportDto>> getBatchReport(@RequestParam String productCode, @RequestParam(required = false) String from, @RequestParam(required = false) String to) {

        DateFormatter dates = getDateFormatter(from, to);

        return ResponseEntity.ok(reportService.getBatchReport(productCode, dates.fromDate(), dates.toDate()));
    }


    private static @NonNull DateFormatter getDateFormatter(String from, String to) {
        LocalDate today = LocalDate.now();

        LocalDate fromDate;
        LocalDate toDate;

        if (from == null && to == null) {
            // Both missing → today only
            fromDate = today;
            toDate = today;
        } else if (from != null && to == null) {
            // Only from provided → to = today
            fromDate = LocalDate.parse(from);
            toDate = today;
        } else {
            // Both provided
            fromDate = LocalDate.parse(from);
            toDate = LocalDate.parse(to);
        }
        DateFormatter dates = new DateFormatter(fromDate, toDate);
        return dates;
    }

    private record DateFormatter(LocalDate fromDate, LocalDate toDate) {
    }
}