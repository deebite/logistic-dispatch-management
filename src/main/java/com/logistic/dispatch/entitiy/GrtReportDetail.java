package com.logistic.dispatch.entitiy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "grt_report_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrtReportDetail extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"ID\"")
    private Long id;

    @Column(name = "\"DATETIME\"")
    private LocalDateTime dateTime;

    @Column(name = "\"SERIAL_NO\"")
    private String serialNo;

    @Column(name = "\"MODEL\"")
    private String model;

    @Column(name = "\"MACHINE\"")
    private String machine;

    @Column(name = "\"STATUS\"")
    private String status;

    @Column(name = "\"OPERATOR\"")
    private String operator;

    @Column(name = "\"SHIFT\"")
    private String shift;

    @Column(name = "\"D1\"")
    private BigDecimal d1;

    @Column(name = "\"D2\"")
    private BigDecimal d2;

    @Column(name = "\"D3\"")
    private BigDecimal d3;

    @Column(name = "\"D4\"")
    private BigDecimal d4;

    @Column(name = "\"D5\"")
    private BigDecimal d5;

    @Column(name = "\"D6\"")
    private BigDecimal d6;

    @Column(name = "\"D7\"")
    private BigDecimal d7;

    @Column(name = "\"D8\"")
    private BigDecimal d8;

    @Column(name = "\"D9\"")
    private BigDecimal d9;

    @Column(name = "\"D10\"")
    private BigDecimal d10;

    @Column(name = "\"D11\"")
    private BigDecimal d11;

    @Column(name = "\"D12\"")
    private BigDecimal d12;

    @Column(name = "\"D13\"")
    private BigDecimal d13;

    @Column(name = "\"D14\"")
    private BigDecimal d14;

    @Column(name = "\"D15\"")
    private BigDecimal d15;

    @Column(name = "\"D16\"")
    private BigDecimal d16;

    @Column(name = "\"D17\"")
    private BigDecimal d17;

    @Column(name = "\"D18\"")
    private BigDecimal d18;

    @Column(name = "\"D19\"")
    private BigDecimal d19;

    @Column(name = "\"D20\"")
    private BigDecimal d20;

    @Column(name = "\"D21\"")
    private BigDecimal d21;

    @Column(name = "\"D22\"")
    private BigDecimal d22;

    @Column(name = "\"D23\"")
    private BigDecimal d23;

    @Column(name = "\"D24\"")
    private BigDecimal d24;

    @Column(name = "\"D25\"")
    private BigDecimal d25;

    @Column(name = "\"D26\"")
    private BigDecimal d26;

    @Column(name = "\"D27\"")
    private BigDecimal d27;

    @Column(name = "\"D28\"")
    private BigDecimal d28;

    @Column(name = "\"D29\"")
    private BigDecimal d29;

    @Column(name = "\"D30\"")
    private BigDecimal d30;

    @Column(name = "\"D31\"")
    private BigDecimal d31;

    @Column(name = "\"D32\"")
    private BigDecimal d32;

    @Column(name = "\"D33\"")
    private BigDecimal d33;

    @Column(name = "\"D34\"")
    private BigDecimal d34;

    @Column(name = "\"D35\"")
    private BigDecimal d35;

    @Column(name = "\"D36\"")
    private BigDecimal d36;

    @Column(name = "\"D37\"")
    private BigDecimal d37;

    @Column(name = "\"D38\"")
    private BigDecimal d38;

    @Column(name = "\"D39\"")
    private BigDecimal d39;

    @Column(name = "\"D40\"")
    private BigDecimal d40;

    @Column(name = "\"StationNo\"")
    private Integer stationNo;
}