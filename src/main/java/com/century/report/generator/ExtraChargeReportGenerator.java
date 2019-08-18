package com.century.report.generator;

import com.century.exception.ExportSalesReportException;
import com.century.report.extra_charge.Invoice;
import com.century.report.ReportSettings;
import com.century.report.ReportType;
import com.century.report.extra_charge.ReportRow;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.century.report.Util.logToFile;
import static com.century.report.extra_charge.Grouping.CLIENT_NAME;
import static com.century.report.extra_charge.Grouping.INVOICE_NUMBER;
import static java.io.File.createTempFile;
import static java.util.stream.Collectors.toList;
import static net.sf.jasperreports.engine.JasperFillManager.fillReport;
import static net.sf.jasperreports.engine.util.JRLoader.loadObject;

@Slf4j
public class ExtraChargeReportGenerator extends ExportSalesReportGenerator<Invoice>{
    public ExtraChargeReportGenerator(ReportType reportType, ReportSettings settings, List<Invoice> data) {
        super(reportType, settings, data);
    }

    @Override
    public File doReport(){
        try {
            JasperEntity entity = getEntity();
            File ret = new File(String.format("%s\\%s.xls",
                    "\\\\alphamess0\\exchange\\spolenov\\export_sales", settings.getFilename()));

            JasperPrint jasperPrint;
            jasperPrint = fillCompiledReport(
                    entity.jasperFields(),
                    entity.jasperParams(),
                    entity.reportName());
            JRXlsExporter jrXlsExporter = new JRXlsExporter();
            jrXlsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            jrXlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(ret));
            jrXlsExporter.setConfiguration(getConfiguration());
            jrXlsExporter.exportReport();

            return ret;
        } catch (Exception e) {
            String msg = String.format("Failed to generate %s report by parameters: %s",
                    reportType, settings);
            logToFile(msg, e);
            throw new ExportSalesReportException(msg, e);
        }
    }

    private JasperPrint fillCompiledReport(List<Map<String, ?>> fields,
                                           Map<String, Object> params,
                                           String templateFileName) throws JRException {
        JasperPrint result = null;
        JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(fields);

        try (InputStream input = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(templateFileName)) {
            JasperReport report = (JasperReport) loadObject(input);
            result = fillReport(report, params, dataSource);
        } catch (IOException ex) {
            String msg = "Failed to fill report";
            logToFile(msg, ex);
            throw new ExportSalesReportException(msg, ex);
        }
        return result;
    }

    private JasperEntity getEntity(){
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", new SimpleDateFormat(DATE_PATTERN).format(settings.getStartDate()));
        params.put("endDate", new SimpleDateFormat(DATE_PATTERN).format(settings.getEndDate()));

        List<Map<String, ?>> fields = prepareFields();
        return new JasperEntity(params, fields, "template/extra_charge.jasper");
    }

    private Stream getSortedStream(Stream<ReportRow> input){
        Stream<ReportRow> temp = input;

        Iterator<String> iter = settings.getGroupings().iterator();
        while(iter.hasNext()){
            String grouping = iter.next();

            if(grouping.equals(CLIENT_NAME.getName())){
                temp = temp.sorted(Comparator.comparing(ReportRow::getClientName,
                        Comparator.nullsFirst(Comparator.naturalOrder())));
            } else if(grouping.equals(INVOICE_NUMBER.getName())){
                temp = temp.sorted(Comparator.comparing(ReportRow::getInvoiceNumber,
                        Comparator.nullsFirst(Comparator.naturalOrder())));
            } else{
                throw new ExportSalesReportException("Unsupported grouping: " + grouping);
            }
        }
        return temp;
    }

    private boolean isSeparateInvoices(){
        return settings.isDetailedByDataElements();
    }

    private List<Map<String, ?>> prepareFields() {
        List<Map<String, ?>> ret = new ArrayList<>();
        List<ReportRow> sortedRows;

        sortedRows = (List<ReportRow>) data.stream().flatMap(
                d -> getSortedStream(d.getReportRows().stream()))
                .collect(Collectors.toList());

        if(!isSeparateInvoices()){
            //Не разделять по накладным, суммировать строки
            sortedRows.forEach(r -> {
                r.setInvoiceNumber(null);
                r.setDateDoc(null);
            });
        }

        for (ReportRow inv : sortedRows) {
            Map<String, Object> field = new HashMap<>();
            ret.add(field);

            field.put("dateDoc", inv.getDateDoc());
            field.put("clientName", inv.getClientName());
            field.put("programName", settings.getProgramName());
            field.put("invoiceNumber", inv.getInvoiceNumber());
            field.put("rowNum", inv.getRowNum());
            field.put("goodsName", inv.getGoodsName());
            field.put("qty", inv.getQty());
            field.put("incomePrice", inv.getIncomePrice());
            field.put("expenditurePrice",inv.getExpenditurePrice());
            field.put("extraCharge", inv.getExtraCharge());
            //put others
        }
        return ret;
    }
}
