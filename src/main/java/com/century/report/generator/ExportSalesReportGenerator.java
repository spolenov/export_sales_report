package com.century.report.generator;

import com.century.exception.ExportSalesReportException;
import com.century.report.ReportSettings;
import com.century.report.ReportType;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static com.century.report.Util.getExcelFileFullPath;
import static com.century.report.Util.logToFile;
import static java.math.RoundingMode.HALF_UP;

public abstract class ExportSalesReportGenerator <T> implements ReportGenerator {
     protected  ReportType reportType;
     protected ReportSettings settings;
     protected List<T> data;

    public ExportSalesReportGenerator(ReportType reportType, ReportSettings settings, List<T> data){
        this.reportType = reportType;
        this.settings = settings;
        this.data = data;

        if(!deleteOutputFileIfExists()){
            logToFile(settings.getUsername(), "Failed to delete output file.");
        }
        verifyInput();
        verifyReportType();
    }

    protected BigDecimal scale(BigDecimal input){
        return input.setScale(settings.getDecimalPlaces(), HALF_UP);
    }

    private boolean deleteOutputFileIfExists(){
        try{
            File existing = new File(getExcelFileFullPath(settings.getFilename()));
            return existing.delete();
        } catch (Exception e) {
            //NOP
        }
        return false;
    }

    private void verifyReportType(){
        if(reportType != ReportType.EXCEL){
            throw new IllegalArgumentException(
                    String.format("Вид отчёта (%s) не поддерживается.", reportType));
        }
    }

    private void verifyInput(){
        if(reportType == null){
            throw new ExportSalesReportException("Не задан тип отчёта.");
        }
        if(settings == null){
            throw new ExportSalesReportException("Не заданы настройки для отчёта.");
        }
        if(data == null){
            throw new ExportSalesReportException("Не заданы входные данные для отчёта.");
        }
    }
}
