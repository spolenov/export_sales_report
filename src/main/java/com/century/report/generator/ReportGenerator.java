package com.century.report.generator;

import net.sf.jasperreports.export.SimpleXlsReportConfiguration;

import java.io.File;

public  interface ReportGenerator {
    String DATE_PATTERN = "dd.MM.yyyy";

    File doReport();

    default SimpleXlsReportConfiguration getConfiguration() {
        SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
        configuration.setDetectCellType(true);
        configuration.setRemoveEmptySpaceBetweenColumns(true);
        configuration.setRemoveEmptySpaceBetweenRows(true);
        return configuration;
    }
}
