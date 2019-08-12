package com.century.report;

import com.century.exception.ExportSalesReportException;

import java.io.File;

public class ReportServiceImpl {
    private static final int REPORT_EMPTY = 4096;

    public void checkFileIsEmpty(File file) throws ExportSalesReportException {
        long length = file.length();
        if (length <= REPORT_EMPTY) {
            throw new ExportSalesReportException("Нет данных для отчёта по выбранным фильтрам.");
        }
    }

}
