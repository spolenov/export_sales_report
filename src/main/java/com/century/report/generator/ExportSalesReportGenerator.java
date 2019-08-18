package com.century.report.generator;

import com.century.exception.ExportSalesReportException;
import com.century.report.ReportSettings;
import com.century.report.ReportType;

import java.io.File;
import java.util.List;

abstract class ExportSalesReportGenerator <T> implements ReportGenerator {
     ReportType reportType;
     ReportSettings settings;
     List<T> data;

    public ExportSalesReportGenerator(ReportType reportType, ReportSettings settings, List<T> data){
        this.reportType = reportType;
        this.settings = settings;
        this.data = data;

        verifyInput();
        verifyReportType();
    }

    public abstract File doReport();

    private void verifyReportType(){
        if(reportType != ReportType.EXCEL){
            throw new IllegalArgumentException(
                    String.format("Report type %s is not supported.",reportType));
        }
    }

    private void verifyInput(){
        if(reportType == null){
            throw new ExportSalesReportException("Report type is null.");
        }
        if(settings == null){
            throw new ExportSalesReportException("Settings are null.");
        }
        if(data == null){
            throw new ExportSalesReportException("Data is null.");
        }
    }
}
