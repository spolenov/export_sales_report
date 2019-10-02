package com.century.report.extra_charge;

import com.century.exception.ExportSalesReportException;
import com.century.report.ReportSettings;
import com.century.report.ReportType;
import com.century.report.generator.ExportSalesReportGenerator;
import com.century.report.generator.JasperEntity;
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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.century.report.Util.*;
import static com.century.report.extra_charge.Grouping.*;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static net.sf.jasperreports.engine.JasperFillManager.fillReport;
import static net.sf.jasperreports.engine.util.JRLoader.loadObject;

@Slf4j
public class ExtraChargeReportGenerator extends ExportSalesReportGenerator<Invoice> {
    public ExtraChargeReportGenerator(ReportType reportType, ReportSettings settings, List<Invoice> data) {
        super(reportType, settings, data);
        prepareGroupings();
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

            if(input == null){
                logToFile(settings.getUsername(), String.format("No such file: %s", templateFileName));
                throw new ExportSalesReportException(String.format("Не существует файл: %s", templateFileName));
            }

            JasperReport report = (JasperReport) loadObject(input);
            result = fillReport(report, params, dataSource);
        } catch (Exception ex) {
            String msg = "Не удалось заполнить данные для отчёта";
            logToFile(settings.getUsername(), msg, ex);
            throw new ExportSalesReportException(msg, ex);
        }
        return result;
    }

    private JasperEntity getEntity(){
        Map<String, Object> params = new HashMap<>();

        //Отрежем накладные, которые не входят в период
        processDataByPeriod();

        if(data.isEmpty()){
            throw new ExportSalesReportException("Нет данных по заданным параметрам.");
        }

        setCommonParams(params);
        setSummaryParams(params);

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
        return new JasperEntity(params, fields, getReportName());
    }

    private void setCommonParams(Map<String, Object> params){
        params.put("startDate", new SimpleDateFormat(DATE_PATTERN).format(settings.getStartDate()));
        params.put("endDate", new SimpleDateFormat(DATE_PATTERN).format(settings.getEndDate()));
        params.put("programName", settings.getProgramName());
        params.put("dateCalc",new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()));
        params.put("isShortReport", isShortReport());
        params.put("decimalPlaces", settings.getDecimalPlaces());
    }

    private void setSummaryParams(Map<String, Object> params){
        params.put("qtySummary", scale(BigDecimal.valueOf(getReportRowsStream()
                .collect(Collectors.summarizingDouble(r -> r.getQty().doubleValue())).getSum())));
        params.put("rowSumSummary", scale(BigDecimal.valueOf(data.stream()
                .collect(Collectors.summarizingDouble(i -> i.getSumma().doubleValue())).getSum())));
    }

    private Stream<ReportRow> getReportRowsStream(){
        return data.stream()
                .flatMap(i -> i.getReportRows().stream());
    }

    private String getReportName(){
        final String folder = "template/";
        String reportName;

        List groupings = settings.getGroupings();

        if(groupings.size() > getMaxGroupingCount()){
            throw new ExportSalesReportException(
                    String.format("Неверное количество группировок: %d", groupings.size()));
        }

        reportName = "extra_charge_" + groupings.size() + (settings.isDetailed()? "": "_short");

        return folder + reportName + ".jasper";
    }

    private Stream<ReportRow> getSortedStream(Stream<ReportRow> input){
        Comparator<ReportRow> comp = (o1, o2) -> 0;

        for (String grouping : settings.getGroupings()) {
            if (grouping.equals(CLIENT_NAME.getName())) {
                comp = comp.thenComparing(ReportRow::getClientName,
                        Comparator.nullsFirst(Comparator.naturalOrder()));

            } else if (grouping.equals(INVOICE_NUMBER.getName())) {
                comp = comp.thenComparing(ReportRow::getInvoiceNumber,
                        Comparator.nullsFirst(Comparator.naturalOrder()));

            } else if (grouping.equals(GOODS_GROUP2.getName())) {
                comp = comp.thenComparing(ReportRow::getGoodsGroup2,
                        Comparator.nullsFirst(Comparator.naturalOrder()));

            } else if (grouping.equals(DATE_DOC.getName())) {
                comp = comp.thenComparing(ReportRow::getDateDoc,
                        Comparator.nullsFirst(Comparator.naturalOrder()));

            } else if (!grouping.equals(NULL.getName())) {
                throw new ExportSalesReportException("Группировка не поддерживается: " + grouping);
            }
        }
        comp = comp.thenComparing(ReportRow::getRowNum);
        return input.sorted(comp);
    }

    private boolean isShortReport(){
        return !settings.isDetailed();
    }

    private List<ReportRow> getReportRows(){
        List<ReportRow> result = getSortedStream(data.stream().flatMap(
                d -> d.getReportRows()
                        .stream()
                        .peek(this::calcFields)))
                .collect(Collectors.toList());

        new ProfitabilityCalculator(settings).setProfitabilityByGroups(result);
        return result;
    }

    private List<Map<String, ?>> prepareFields() {
        List<Map<String, ?>> ret = new ArrayList<>();
        List<ReportRow> sortedRows;

        sortedRows = getReportRows();

        for (ReportRow inv : sortedRows) {
            Map<String, Object> field = new HashMap<>();
            ret.add(field);

            field.put("dateDoc", inv.getDateDoc());
            field.put("clientName", inv.getClientName());
            field.put("invoiceNumber", inv.getInvoiceNumber());
            field.put("invoiceSum", inv.getSumma());
            field.put("rowNum", inv.getRowNum());
            field.put("rowSum", inv.getRowSum());
            field.put("goodsName", inv.getGoodsName());
            field.put("qty", inv.getQty());
            field.put("incomePrice", inv.getIncomePrice());
            field.put("expenditurePrice", inv.getExpenditurePrice());
            field.put("extraCharge1C", inv.getExtraCharge1C());
            field.put("extraChargeExport", inv.getExtraChargeExport());
            field.put("incomePriceWithoutVAT", inv.getIncomePriceWithoutVAT());
            field.put("goodsGroup2", inv.getGoodsGroup2());
            field.put("margin", inv.getMargin());
            field.put("profitability", inv.getProfitability());
            field.put("marginWithoutVAT", inv.getMarginWithoutVAT());
            field.put("profitabilityWithoutVAT", inv.getProfitabilityWithoutVAT());

            field.put("profitabilityByGroup1", inv.getProfitabilityByGroup1());
            field.put("profitabilityWithoutVATByGroup1", inv.getProfitabilityWithoutVATByGroup1());

            field.put("profitabilityByGroup2", inv.getProfitabilityByGroup2());
            field.put("profitabilityWithoutVATByGroup2", inv.getProfitabilityWithoutVATByGroup2());
        }
        return ret;
    }

    private void calcFields(ReportRow row){
        row.setExtraChargeExport(
                row.getIncomePrice().compareTo(ZERO) <= 0?
                ZERO: row.getExpenditurePrice().subtract(row.getIncomePrice())
                .divide(row.getIncomePrice(), getScale(), HALF_UP));

        row.setIncomePriceWithoutVAT (
                row.getIncomePrice().divide(
                    new BigDecimal(row.getVat())
                            .divide(new BigDecimal(100), getScale(), HALF_UP)
                            .add(BigDecimal.ONE), 15, HALF_UP));

        row.setExtraCharge1C(row.getIncomePriceWithoutVAT().compareTo(ZERO) <=0 ? ZERO:
                row.getExpenditurePrice().subtract(row.getIncomePriceWithoutVAT())
                .divide(row.getIncomePriceWithoutVAT(), settings.getDecimalPlaces(), HALF_UP));

        row.setMargin(
                row.getRowSum().subtract(row.getIncomePrice().multiply(row.getQty())));

        row.setMarginWithoutVAT(
                row.getRowSum().subtract(row.getIncomePriceWithoutVAT().multiply(row.getQty())));

        row.setProfitability(row.getRowSum() == null || row.getRowSum().equals(ZERO)? ZERO:
                row.getMargin().divide(
                        row.getRowSum(), settings.getDecimalPlaces(), HALF_UP));

        row.setProfitabilityWithoutVAT(row.getRowSum() == null || row.getRowSum().equals(ZERO)? ZERO:
                row.getMarginWithoutVAT().divide(
                        row.getRowSum(), settings.getDecimalPlaces(), HALF_UP));
    }

    private void prepareGroupings(){
        this.settings.getGroupings()
                .removeIf(g -> g == null || g.equals("null") || g.isEmpty());
    }

    private void processDataByPeriod(){
        this.data.removeIf(d -> d.getDateDoc().getTime() < settings.getStartDate().getTime() ||
                d.getDateDoc().getTime() >= settings.getEndDate().getTime());
    }
}
