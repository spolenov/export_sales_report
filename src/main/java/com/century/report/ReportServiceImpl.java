package com.century.report;

import com.century.exception.ExportSalesReportException;
import com.century.report.extra_charge.ExtraChargeReportGenerator;
import com.century.report.extra_charge.Invoice;
import com.century.report.generator.ReportGenerator;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

import static com.century.report.ReportName.EXTRA_CHARGE;
import static com.century.report.Util.*;

@Slf4j
public class ReportServiceImpl {
    private static ReportSettings settings = getSettings();

    public void doReport(ReportName reportName, ReportType type) {
        String username = settings.getUsername();

        Util.logToFile(username,
                String.format(
                        "Started %s report generation. Format: %s",
                        reportName, type));

        if(reportName.equals(EXTRA_CHARGE)){
            List<Invoice> invoices = getInvoices(settings);
            doReport(reportName, type, settings, invoices);
            return;
        }

        logToFile(username, "Unknown report name: " + reportName);
        throw new ExportSalesReportException("Неизвестное имя отчёта: " + reportName);
    }

    private List<Invoice> getInvoices(ReportSettings settings){
        try{
            return parseInvoices();
        } catch (Exception e){
            logToFile(settings.getUsername(), "Failed to get invoices from json file", e);
            throw new ExportSalesReportException(e);
        }
    }

    private void doReport(ReportName reportName,
                          ReportType type,
                          ReportSettings settings,
                          List<Invoice> invoices){
        try{
            File result =  doReportExtraCharge(type, settings, invoices);

            if(result == null){
                throw new ExportSalesReportException("Не удалось сгенерировать результирующий файл Excel.");
            }

            logToFile(settings.getUsername(),
                    String.format(
                            "Finished %s report generation. Format: %s. File size is %d KB",
                            reportName, type, result.length()/ (1024)));

        } catch (Exception e){
            logToFile(settings.getUsername(),
                    String.format("Failed to generate report (%s)",
                    reportName), e);
            throw new ExportSalesReportException(e);
        }
    }

    private static ReportSettings getSettings() {
        try{
            settings = parseSettings();
            validateSettings();
        } catch (Exception e){
            logToFile("Failed to parse settings json file", e);
            throw new ExportSalesReportException(e);
        }
        return settings;
    }

    private static void validateSettings(){
        int groupingsCount = settings.getGroupings().size();

        if(groupingsCount > getMaxGroupingCount()){
            throw new ExportSalesReportException(
                    String.format("Полученное количество группировок: %d, максимально допустимое - %d.",
                            groupingsCount, getMaxGroupingCount()));
        }

        if(settings.getStartDate() == null ||
                settings.getEndDate() == null ||
                settings.getEndDate().before(settings.getStartDate())){
            throw new ExportSalesReportException("Неверный интервал дат для отчёта.");
        }
    }

    private File doReportExtraCharge(ReportType type, ReportSettings settings, List<Invoice> invoices){
        ReportGenerator generator = new ExtraChargeReportGenerator(type, settings, invoices);
        return generator.doReport();
    }
}
