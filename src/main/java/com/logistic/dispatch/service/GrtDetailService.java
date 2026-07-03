package com.logistic.dispatch.service;

import com.logistic.dispatch.dto.GrtReportRequestDto;
import com.logistic.dispatch.dto.GrtReportResponseDto;

public interface GrtDetailService {

    GrtReportResponseDto createGrtReport(GrtReportRequestDto dto);
}
