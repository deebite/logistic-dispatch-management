package com.logistic.dispatch.controller;

import com.logistic.dispatch.dto.BatchReportDto;
import com.logistic.dispatch.dto.DispatchSummaryResponseDto;
import com.logistic.dispatch.dto.ProductSummaryDto;
import com.logistic.dispatch.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.jspecify.annotations.NonNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/product-summary")
    public ResponseEntity<ProductSummaryDto> getProductSummary(@RequestParam String productCode, @RequestParam(required = false) String from, @RequestParam(required = false) String to) {

        DateFormatter dates = getDateFormatter(from, to);

        return ResponseEntity.ok(reportService.getProductSummary(productCode, dates.fromDate(), dates.toDate()));
    }


    @GetMapping("/batch-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
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

    @GetMapping("/{productCode}/dispatch-summary")
    public ResponseEntity<DispatchSummaryResponseDto> getDispatchSummary(@PathVariable String productCode, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws BadRequestException {

        // No dates -> Today
        if (startDate == null && endDate == null) {
            startDate = LocalDate.now();
            endDate = LocalDate.now();
        }
        // Start date provided, end date missing -> Today
        else if (startDate != null && endDate == null) {
            endDate = LocalDate.now();
        }
        // End date provided, start date missing -> Same day
        else if (startDate == null) {
            startDate = endDate;
        }

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date.");
        }

        return ResponseEntity.ok(
                reportService.getDispatchSummary(
                        productCode,
                        startDate,
                        endDate
                )
        );
    }
}