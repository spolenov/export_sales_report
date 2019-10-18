package com.century.report.generator;

import com.century.exception.ExportSalesReportException;
import com.century.report.ReportSettings;
import com.century.report.ReportType;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static com.century.report.service.util.UtilService.getResultFileName;
import static java.math.RoundingMode.HALF_UP;

public abstract class ExportSalesReportGenerator <T> implements ReportGenerator {
    protected  ReportType reportType;
    protected ReportSettings settings;
    protected List<T> data;

    public ExportSalesReportGenerator(ReportType reportType, ReportSettings settings, List<T> data){
        this.reportType = reportType;
        this.settings = settings;
        this.data = data;

        setFileName();
        verifyInput();
        verifyReportType();
    }

    protected BigDecimal scale(BigDecimal input){
        return input.setScale(settings.getDecimalPlaces(), HALF_UP);
    }

    protected abstract void logToFile(String username, String msg);

    private void setFileName(){
        String filename = getResultFileName();

        if(!deleteOutputFileIfExists(filename)){
            logToFile(settings.getUsername(), "Failed to delete output file.");
        }
        this.settings.setFilename(filename);
    }

    private boolean deleteOutputFileIfExists(String filename){
        try{
            File existing = new File(getExcelFileFullPath(filename));
            if(existing.exists()){
                return existing.delete();
            }
            return true;
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

    protected abstract String getExcelFileFullPath(String filename);
}
