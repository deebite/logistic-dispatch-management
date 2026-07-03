package com.logistic.dispatch.controller;

import com.logistic.dispatch.dto.GrtReportRequestDto;
import com.logistic.dispatch.dto.GrtReportResponseDto;
import com.logistic.dispatch.service.GrtDetailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grt-report")
public class GrtReportController {

    private final GrtDetailService grtReportService;

    public GrtReportController(GrtDetailService grtReportService) {
        this.grtReportService = grtReportService;
    }

    @PostMapping("/create")
    public ResponseEntity<GrtReportResponseDto> createReport(@Valid @RequestBody GrtReportRequestDto dto) {
        return ResponseEntity.ok(grtReportService.createGrtReport(dto));
    }
}
