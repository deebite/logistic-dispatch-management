package com.logistic.dispatch.service.impl;

import com.logistic.dispatch.dto.GrtReportRequestDto;
import com.logistic.dispatch.dto.GrtReportResponseDto;
import com.logistic.dispatch.entitiy.GrtReportDetail;
import com.logistic.dispatch.mapper.GrtReportMapper;
import com.logistic.dispatch.repository.GrtReportRepository;
import com.logistic.dispatch.service.GrtDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GrtDetailServiceImpl implements GrtDetailService {

    private final GrtReportRepository grtReportRepository;
    private static final Logger LOG = LoggerFactory.getLogger(GrtDetailServiceImpl.class);

    public GrtDetailServiceImpl(GrtReportRepository grtReportRepository) {
        this.grtReportRepository = grtReportRepository;
    }

    @Override
    public GrtReportResponseDto createGrtReport(GrtReportRequestDto dto) {
        LOG.info("Creating GRT report with details: {}", dto);
        GrtReportDetail report = GrtReportMapper.toEntity(dto);

        if (report.getDateTime() == null) {
            report.setDateTime(LocalDateTime.now());
        }

        GrtReportDetail saved = grtReportRepository.save(report);

        return GrtReportMapper.toResponse(saved);
    }
}
