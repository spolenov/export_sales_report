package com.century.report.generator;

import com.century.exception.ExportSalesReportException;
import com.century.report.ReportSettings;
import com.century.report.ReportType;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static com.century.report.Util.getExcelFileFullPath;
import static java.math.RoundingMode.HALF_UP;

public abstract class ExportSalesReportGenerator <T> implements ReportGenerator {
     protected  ReportType reportType;
     protected ReportSettings settings;
     protected List<T> data;

    public ExportSalesReportGenerator(ReportType reportType, ReportSettings settings, List<T> data){
        this.reportType = reportType;
        this.settings = settings;
        this.data = data;

        deleteOutputFileIfExists();
        verifyInput();
        verifyReportType();
    }

    public abstract File doReport();

    protected BigDecimal scale(BigDecimal input){
        return input.setScale(settings.getDecimalPlaces(), HALF_UP);
    }

    private void deleteOutputFileIfExists(){
        try{
            File existing = new File(getExcelFileFullPath(settings.getFilename()));
            existing.delete();
        } catch (Exception e){
            //NOP
        }
    }

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
