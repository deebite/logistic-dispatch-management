package com.logistic.dispatch.mapper;

import com.logistic.dispatch.dto.GrtReportRequestDto;
import com.logistic.dispatch.dto.GrtReportResponseDto;
import com.logistic.dispatch.entitiy.GrtReportDetail;

public class GrtReportMapper {

    public static GrtReportDetail toEntity(GrtReportRequestDto dto) {

        GrtReportDetail report = new GrtReportDetail();

        report.setDateTime(dto.getDateTime());
        report.setSerialNo(dto.getSerialNo());
        report.setModel(dto.getModel());
        report.setMachine(dto.getMachine());
        report.setStatus(dto.getStatus());
        report.setOperator(dto.getOperator());
        report.setShift(dto.getShift());

        report.setD1(dto.getD1());
        report.setD2(dto.getD2());
        report.setD3(dto.getD3());
        report.setD4(dto.getD4());
        report.setD5(dto.getD5());
        report.setD6(dto.getD6());
        report.setD7(dto.getD7());
        report.setD8(dto.getD8());
        report.setD9(dto.getD9());
        report.setD10(dto.getD10());

        report.setD11(dto.getD11());
        report.setD12(dto.getD12());
        report.setD13(dto.getD13());
        report.setD14(dto.getD14());
        report.setD15(dto.getD15());
        report.setD16(dto.getD16());
        report.setD17(dto.getD17());
        report.setD18(dto.getD18());
        report.setD19(dto.getD19());
        report.setD20(dto.getD20());

        report.setD21(dto.getD21());
        report.setD22(dto.getD22());
        report.setD23(dto.getD23());
        report.setD24(dto.getD24());
        report.setD25(dto.getD25());
        report.setD26(dto.getD26());
        report.setD27(dto.getD27());
        report.setD28(dto.getD28());
        report.setD29(dto.getD29());
        report.setD30(dto.getD30());

        report.setD31(dto.getD31());
        report.setD32(dto.getD32());
        report.setD33(dto.getD33());
        report.setD34(dto.getD34());
        report.setD35(dto.getD35());
        report.setD36(dto.getD36());
        report.setD37(dto.getD37());
        report.setD38(dto.getD38());
        report.setD39(dto.getD39());
        report.setD40(dto.getD40());

        report.setStationNo(dto.getStationNo());

        return report;
    }

    public static GrtReportResponseDto toResponse(GrtReportDetail report) {

        return new GrtReportResponseDto(
                report.getSerialNo(),
                report.getModel(),
                report.getMachine(),
                report.getStatus(),
                report.getOperator(),
                report.getShift(),
                "GRT Report created successfully"
        );
    }
}