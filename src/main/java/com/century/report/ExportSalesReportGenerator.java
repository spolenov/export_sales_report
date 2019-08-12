package com.century.report;

import java.io.File;

public class ExportSalesReportGenerator <T> implements ReportGenerator{
    private ReportType reportType;
    private ReportSettings settings;
    private T data;

    public ExportSalesReportGenerator(ReportType reportType, ReportSettings settings, T data){
        this.reportType = reportType;
        this.settings = settings;
        this.data = data;

        verifyReportType();
    }

    public File doReport() {
        return null;
    }

    private void verifyReportType(){
        if(reportType != ReportType.EXCEL){
            throw new IllegalArgumentException(
                    String.format("Report type %s is not supported.",reportType));
        }
    }
}
