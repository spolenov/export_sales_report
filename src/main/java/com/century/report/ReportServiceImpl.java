package com.century.report;

import com.century.exception.ExportSalesReportException;
import com.century.report.extra_charge.Invoice;
import com.century.report.generator.ExtraChargeReportGenerator;
import com.century.report.generator.ReportGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

import static com.century.report.ReportName.EXTRA_CHARGE;
import static com.century.report.Util.*;

@Slf4j
public class ReportServiceImpl {
    public void doReport(ReportName reportName, ReportType type) {
        ReportSettings settings = getSettings();
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
        throw new ExportSalesReportException("Unknown report name: " + reportName);
    }

    private List<Invoice> getInvoices(ReportSettings settings){
        try{
            return Util.getInvoices();
        } catch (Exception e){
            logToFile(settings.getUsername(), "Failed to get invoices from json file", e);
            //throw new ExportSalesReportException(e);

            try{
                return getResourceObject("invoice.json", new TypeReference<List<Invoice>>(){});
            } catch (Exception e1){
                throw new ExportSalesReportException(e1);
            }
        }
    }

    private void doReport(ReportName reportName,
                          ReportType type,
                          ReportSettings settings,
                          List<Invoice> invoices){
        try{
            File result =  doReportExtraCharge(type, settings, invoices);

            if(result == null){
                throw new ExportSalesReportException("Result file is null");
            }

            Util.logToFile(settings.getUsername(),
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
        ReportSettings settings;
        try{
            settings = parseSettings();
        } catch (Exception e){
            logToFile("Failed to parse settings json file", e);
            //throw new ExportSalesReportException(e);

            try{
                settings = getResourceObject(
                        "settings.json",
                        new TypeReference<ReportSettings>(){});
            } catch (Exception e1){
                throw new ExportSalesReportException(e1);
            }
        }
        return settings;
    }

    private File doReportExtraCharge(ReportType type, ReportSettings settings, List<Invoice> invoices){
        ReportGenerator generator = new ExtraChargeReportGenerator(type, settings, invoices);
        return generator.doReport();
    }

}
