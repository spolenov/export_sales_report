package com.century.report.generator;

import com.century.exception.ExportSalesReportException;
import com.century.report.ReportSettings;
import com.century.report.ReportType;
import com.century.report.extra_charge.Grouping;
import com.century.report.extra_charge.Invoice;
import com.century.report.extra_charge.ReportRow;
import com.century.report.util.BigDecimalAverageCollector;
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
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.century.report.Util.*;
import static com.century.report.extra_charge.Grouping.*;
import static java.util.stream.Collectors.groupingBy;
import static net.sf.jasperreports.engine.JasperFillManager.fillReport;
import static net.sf.jasperreports.engine.util.JRLoader.loadObject;

@Slf4j
public class ExtraChargeReportGenerator extends ExportSalesReportGenerator<Invoice>{
    public ExtraChargeReportGenerator(ReportType reportType, ReportSettings settings, List<Invoice> data) {
        super(reportType, settings, data);
    }

    @Override
    public File doReport(){
        File ret = null;

        try {
            JasperEntity entity = getEntity();
            ret = new File(getExcelFileFullPath(settings.getFilename()));

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
            String msg = String.format("Failed to generate %s report file (%s) by parameters: %s",
                    reportType, ret == null? "null": ret.getAbsolutePath(), settings);
            logToFile(settings.getUsername(), msg, e);
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
        params.put("programName", settings.getProgramName());
        params.put("dateCalc",new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()));
        params.put("isShortReport", !settings.isDetailedByDataElements());

        Iterator<String> groupIterator = settings.getGroupings().iterator();

        for(int i = 1; i<= getMaxGroupingCount(); i++){
            if(groupIterator.hasNext()){
                params.put("grouping" + i, groupIterator.next());
            } else{
                params.put("grouping" + i, "null");
            }
        }
        log.info("Report params are {}", params);

        List<Map<String, ?>> fields = prepareFields();
        return new JasperEntity(params, fields, "template/extra_charge.jasper");
    }

    private Stream<ReportRow> getSortedStream(Stream<ReportRow> input){
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
            } else if(grouping.equals(GOODS_GROUP2.getName())){
                temp = temp.sorted(Comparator.comparing(ReportRow::getGoodsGroup2,
                        Comparator.nullsFirst(Comparator.naturalOrder())));
            } else if(grouping.equals(DATE_DOC.getName())){
                temp = temp.sorted(Comparator.comparing(ReportRow::getDateDoc,
                        Comparator.nullsFirst(Comparator.naturalOrder())));
            } else if(!grouping.equals(Grouping.NULL.getName())){
                throw new ExportSalesReportException("Unsupported grouping: " + grouping);
            }
        }
        return temp;
    }

    private boolean isSeparateInvoices(){
        return settings.isDetailedByDataElements();
    }

    private List<ReportRow> getReportRows(){
        return data.stream().flatMap(
                d -> getSortedStream(d.getReportRows()
                        .stream()
                        .peek(this::calcFields)))
                .collect(Collectors.toList());
    }

    private List<Map<String, ?>> prepareFields() {
        List<Map<String, ?>> ret = new ArrayList<>();
        List<ReportRow> sortedRows;

        if(!isSeparateInvoices()){
            sortedRows = getGroupedRowsForShortReport(getReportRows());
        } else {
            sortedRows = getReportRows();
        }

        for (ReportRow inv : sortedRows) {
            Map<String, Object> field = new HashMap<>();
            ret.add(field);

            field.put("dateDoc", inv.getDateDoc());
            field.put("clientName", inv.getClientName());
            field.put("invoiceNumber", inv.getInvoiceNumber());
            field.put("rowNum", inv.getRowNum());
            field.put("rowSum", inv.getRowSum());
            field.put("goodsName", inv.getGoodsName());
            field.put("qty", inv.getQty());
            field.put("incomePrice", inv.getIncomePrice());
            field.put("expenditurePrice",inv.getExpenditurePrice());
            field.put("extraCharge1C", inv.getExtraCharge1C());
            field.put("extraChargeExport", inv.getExtraChargeExport());
            field.put("incomePriceWithoutVAT", inv.getIncomePriceWithoutVAT());
            field.put("goodsGroup2", inv.getGoodsGroup2());
            //put others
        }
        return ret;
    }

    private void calcFields(ReportRow row){
        row.setExtraCharge1C(row.getExpenditurePrice().subtract(row.getIncomePriceWithoutVAT())
                .divide(row.getExpenditurePrice(), 2, RoundingMode.HALF_UP));
        row.setExtraChargeExport(row.getExpenditurePrice().subtract(row.getIncomePrice())
                .divide(row.getIncomePrice(), 2, RoundingMode.HALF_UP));
    }

    private List<ReportRow> getGroupedRowsForShortReport(List<ReportRow> inputRows){
        List<ReportRow> result = new ArrayList<>();

        //Из отдельных строк накладных получить суммарные строки
        Map<String, List<ReportRow>> groupedRows = inputRows.stream()
                .collect(groupingBy(ReportRow::getInvoiceNumber));

        groupedRows.forEach((key, rows) -> {
            ReportRow firstRow = rows.iterator().next();
            ReportRow groupedRow = new ReportRow();

            groupedRow.setInvoiceNumber(key);
            groupedRow.setClientId(firstRow.getClientId());

            groupedRow.setExtraCharge1C(rows.stream()
                    .map(ReportRow::getExtraCharge1C)
                    .collect(new BigDecimalAverageCollector()));
            groupedRow.setExtraChargeExport(rows.stream()
                    .map(ReportRow::getExtraChargeExport)
                    .collect(new BigDecimalAverageCollector()));

            result.add(groupedRow);
        });

        return getSortedStream(result.stream()).collect(Collectors.toList());
    }
}
