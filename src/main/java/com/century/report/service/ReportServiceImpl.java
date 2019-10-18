package com.century.report.service;

import com.century.exception.ExportSalesReportException;
import com.century.report.*;
import com.century.report.extra_charge.model.Invoice;
import com.century.report.extra_charge.service.ExtraChargeReportGenerator;
import com.century.report.generator.ReportGenerator;
import com.century.report.service.util.UtilService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

import static com.century.report.ReportName.EXTRA_CHARGE;
import static com.century.report.StringResult.okResult;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {
    private ReportSettings settings;

    @Autowired
    private TxtLogger logger;

    @Autowired
    private UtilService utilService;

    public StringResult doReport(ReportName reportName,
                                 ReportType type,
                                 ReportRequest<Invoice> request) {
        try{
            this.settings = getSettings(request);
            String username = settings.getUsername();

            logger.logToFile(username,
                    String.format(
                            "Started %s report generation. Format: %s",
                            reportName, type));

            if(reportName.equals(EXTRA_CHARGE)){
                return doReportExtraCharge(request, type);
            }

            logger.logToFile(username, "Unknown report name: " + reportName);
            throw new ExportSalesReportException("Неизвестное имя отчёта: " + reportName);
        } catch (Exception e){
            log.error(String.format(
                    "Failed to generate %s. Format: %s.",
                    reportName, type), e);

            logger.logToFile(String.format(
                            "Failed to generate %s. Format: %s. Error: %s",
                            reportName, type, e.getMessage()));
            return StringResult.errResult(e);
        }
    }

    private StringResult doReportExtraCharge(ReportRequest<Invoice> request, ReportType type){
        List<Invoice> invoices;
        try{
            invoices = request.getDataAs(Invoice.class);
        } catch (Exception e){
            logger.logToFile(settings.getUsername(), "Failed to get invoices from json file", e);
            throw new ExportSalesReportException(e);
        }

        File result = null;

        try{
            result = doReportExtraCharge(type, settings, invoices);

            if(result == null){
                throw new ExportSalesReportException("Не удалось сгенерировать результирующий файл Excel.");
            }

            logger.logToFile(settings.getUsername(),
                    String.format(
                            "Finished %s report generation. Format: %s. File size is %d KB",
                            EXTRA_CHARGE, type, result.length()/ (1024)));

            String remoteFilePath = utilService.transferToFTP(result);

            if(remoteFilePath.isEmpty()){
                throw new ExportSalesReportException("Не удалось копировать файл на FTP.");
            }

            settings.setFilename(remoteFilePath);

        } catch (Exception e){
            logger.logToFile(settings.getUsername(),
                    String.format("Failed to generate report (%s)",
                            EXTRA_CHARGE), e);
            throw new ExportSalesReportException(e);
        } finally {
            if(result != null && result.exists()){
                try{
                    result.delete();
                } catch (Exception e){
                    logger.logToFile("Failed to delete local excel file: ", e);
                }
            }
        }
        logger.logToFile("Successfully generated extra charge report.");
        return okResult(settings.getFilename());
    }


    private ReportSettings getSettings(ReportRequest request) {
        try{
            settings = request.getSettings();
            validateSettings();
        } catch (Exception e){
            logger.logToFile("Failed to parse settings json file", e);
            throw new ExportSalesReportException(e);
        }

        return settings;
    }

    private void validateSettings(){
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
