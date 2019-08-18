package com.century;

import com.century.report.ReportServiceImpl;
import lombok.extern.slf4j.Slf4j;

import static com.century.report.ReportName.EXTRA_CHARGE;
import static com.century.report.ReportType.EXCEL;

@Slf4j
public class Main {
    public static void main(String[] args) {
        try {
            new ReportServiceImpl().doReport(EXTRA_CHARGE, EXCEL);
        } catch (Exception e) {
            log.error("Error in main:", e);
        }
    }
}
