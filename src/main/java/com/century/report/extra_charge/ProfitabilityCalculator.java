package com.century.report.extra_charge;

import com.century.exception.ExportSalesReportException;
import com.century.report.ReportSettings;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.century.report.extra_charge.Grouping.*;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

class ProfitabilityCalculator {
    private ReportSettings settings;

    ProfitabilityCalculator(ReportSettings settings){
        this.settings = settings;
    }
    
    private int getDecimalPlaces(){
        return settings.getDecimalPlaces() + 2;
    }
    
    private void putToProfitabilityMap(Map<String, BigDecimal> map, String group, BigDecimal invoiceSumInGroup, BigDecimal marginInGroup){
        if(invoiceSumInGroup.equals(ZERO)){
            return;
        }
        map.put(group, marginInGroup.divide(
                invoiceSumInGroup, this.getDecimalPlaces(), HALF_UP));
    }

    private void setInvoiceNumProfitability(List<ReportRow> rows, int groupingOrder){
        Map<String, BigDecimal> profitabilityMap = new HashMap<>();
        Map<String, BigDecimal> profitabilityWithoutVATMap = new HashMap<>();

        Set<String> groupValues = getGroupValues(rows, INVOICE_NUMBER.getName());

        for(String group: groupValues){

            BigDecimal invoiceSumInGroup = ZERO;
            BigDecimal marginInGroup = ZERO;
            BigDecimal marginWithoutVATInGroup = ZERO;

            for(ReportRow row: rows){
                if(row.getInvoiceNumber().equals(group)){
                    invoiceSumInGroup = invoiceSumInGroup.add(row.getRowSum());
                    marginInGroup = marginInGroup.add(row.getMargin());
                    marginWithoutVATInGroup = marginWithoutVATInGroup.add(row.getMarginWithoutVAT());
                }
            }

            putToProfitabilityMap(profitabilityMap, group, invoiceSumInGroup, marginInGroup);
            putToProfitabilityMap(profitabilityWithoutVATMap, group, invoiceSumInGroup, marginWithoutVATInGroup);
        }

        if(groupingOrder == 1){
            rows.forEach(row -> row.setProfitabilityByGroup1(
                    profitabilityMap.get(row.getInvoiceNumber())));
            rows.forEach(row -> row.setProfitabilityWithoutVATByGroup1(
                    profitabilityWithoutVATMap.get(row.getInvoiceNumber())));
        }
        if(groupingOrder == 2){
            rows.forEach(row -> row.setProfitabilityByGroup2(
                    profitabilityMap.get(row.getInvoiceNumber())));
            rows.forEach(row -> row.setProfitabilityWithoutVATByGroup2(
                    profitabilityWithoutVATMap.get(row.getInvoiceNumber())));
        }
    }

    private void setClientProfitability(List<ReportRow> rows, int groupingOrder){
        Map<String, BigDecimal> profitabilityMap = new HashMap<>();
        Map<String, BigDecimal> profitabilityWithoutVATMap = new HashMap<>();

        Set<String> groupValues = getGroupValues(rows, CLIENT_NAME.getName());

        for(String group: groupValues){
            BigDecimal invoiceSumInGroup = ZERO;
            BigDecimal marginInGroup = ZERO;
            BigDecimal marginWithoutVATInGroup = ZERO;

            for(ReportRow row: rows){
                if(row.getClientName().equals(group)){
                    invoiceSumInGroup = invoiceSumInGroup.add(row.getRowSum());
                    marginInGroup = marginInGroup.add(row.getMargin());
                    marginWithoutVATInGroup = marginWithoutVATInGroup.add(row.getMarginWithoutVAT());
                }
            }
            putToProfitabilityMap(profitabilityMap, group, invoiceSumInGroup, marginInGroup);
            putToProfitabilityMap(profitabilityWithoutVATMap, group, invoiceSumInGroup, marginWithoutVATInGroup);
        }

        if(groupingOrder == 1){
            rows.forEach(row -> row.setProfitabilityByGroup1(
                    profitabilityMap.get(row.getClientName())));
            rows.forEach(row -> row.setProfitabilityWithoutVATByGroup1(
                    profitabilityWithoutVATMap.get(row.getClientName())));
        }
        if(groupingOrder == 2){
            rows.forEach(row -> row.setProfitabilityByGroup2(
                    profitabilityMap.get(row.getClientName())));
            rows.forEach(row -> row.setProfitabilityWithoutVATByGroup2(
                    profitabilityWithoutVATMap.get(row.getClientName())));
        }
    }

    private Object getValueFromRow(ReportRow row, String groupName){
        if(groupName.equals(INVOICE_NUMBER.getName())){
            return row.getInvoiceNumber();
        }

        if(groupName.equals(GOODS_GROUP2.getName())){
            return row.getGoodsGroup2();
    }

        if(groupName.equals(CLIENT_NAME.getName())){
            return row.getClientName();
        }

        throw new ExportSalesReportException("Неизвестная группировка: " + groupName);
    }

    private void setInvoiceNumProfitability(List<ReportRow> rows,
                                               String previousGroupName,
                                               int groupingOrder){
        setProfitability(rows, previousGroupName, INVOICE_NUMBER.getName(), groupingOrder);
    }

    private void setGoodsGroup2Profitability(List<ReportRow> rows,
                                             String previousGroupName,
                                             int groupingOrder){

        setProfitability(rows, previousGroupName, GOODS_GROUP2.getName(), groupingOrder);
    }

    private void setClientProfitability(List<ReportRow> rows,
                                        String previousGroupName,
                                        int groupingOrder){

        setProfitability(rows, previousGroupName, CLIENT_NAME.getName(), groupingOrder);
    }

    private void setProfitability(List<ReportRow> rows,
                                  String previousGroupName,
                                  String currentGroupName,
                                  int groupingOrder){
        Map<String, BigDecimal> profitabilityMap = new HashMap<>();
        Map<String, BigDecimal> profitabilityWithoutVATMap = new HashMap<>();

        Set<String> previousGroupValues = getGroupValues(rows, previousGroupName);

        for(String previousGroupValue: previousGroupValues){
            Set<String> groupValues = getGroupValues(rows, currentGroupName);

            for (String group: groupValues){
                BigDecimal invoiceSumInGroup = ZERO;
                BigDecimal marginInGroup = ZERO;
                BigDecimal marginWithoutVATInGroup = ZERO;

                for(ReportRow row: rows){
                    if(getValueFromRow(row, currentGroupName).equals(group) && getValueFromRow(row, previousGroupName).equals(previousGroupValue)){
                        invoiceSumInGroup = invoiceSumInGroup.add(row.getRowSum());
                        marginInGroup = marginInGroup.add(row.getMargin());
                        marginWithoutVATInGroup = marginWithoutVATInGroup.add(row.getMarginWithoutVAT());
                    }
                }
                putToProfitabilityMap(profitabilityMap, previousGroupValue + "_" + group, invoiceSumInGroup, marginInGroup);
                putToProfitabilityMap(profitabilityWithoutVATMap, previousGroupValue + "_" + group, invoiceSumInGroup, marginWithoutVATInGroup);
            }
        }

        setProfitability(rows, profitabilityMap, profitabilityWithoutVATMap, previousGroupName, currentGroupName, groupingOrder);
    }

    private void setProfitability(List<ReportRow> rows,
                                  Map<String, BigDecimal> profitabilityMap,
                                  Map<String, BigDecimal> profitabilityWithoutVATMap,
                                  String previousGroupName,
                                  String currentGroupName,
                                  int groupingOrder){
        if(groupingOrder == 1){
            rows.forEach(row -> row.setProfitabilityByGroup1(
                    profitabilityMap.get(getValueFromRow(row, previousGroupName) + "_" + getValueFromRow(row, currentGroupName))));
            rows.forEach(row -> row.setProfitabilityWithoutVATByGroup1(
                    profitabilityWithoutVATMap.get(getValueFromRow(row, previousGroupName) + "_" + getValueFromRow(row, currentGroupName))));
        }
        if(groupingOrder == 2){
            rows.forEach(row -> row.setProfitabilityByGroup2(
                    profitabilityMap.get(getValueFromRow(row, previousGroupName) + "_" + getValueFromRow(row, currentGroupName))));
            rows.forEach(row -> row.setProfitabilityWithoutVATByGroup2(
                    profitabilityWithoutVATMap.get(getValueFromRow(row, previousGroupName) + "_" + getValueFromRow(row, currentGroupName))));
        }
    }

    private Set<String> getGroupValues(List<ReportRow> rows, String groupName){
        if(groupName.equals(INVOICE_NUMBER.getName())){
            return rows.stream()
                    .map(ReportRow::getInvoiceNumber)
                    .collect(Collectors.toSet());
        }

        if(groupName.equals(GOODS_GROUP2.getName())){
            return rows.stream()
                    .map(ReportRow::getGoodsGroup2)
                    .collect(Collectors.toSet());
        }

        if(groupName.equals(CLIENT_NAME.getName())){
            return rows.stream()
                    .map(ReportRow::getClientName)
                    .collect(Collectors.toSet());
        }
        throw new ExportSalesReportException("Неизвестная группировка: " + groupName);
    }

    private void setGoodsGroup2Profitability(List<ReportRow> rows, int groupingOrder){
        Map<String, BigDecimal> profitabilityMap = new HashMap<>();
        Map<String, BigDecimal> profitabilityWithoutVATMap = new HashMap<>();

        Set<String> groupValues = getGroupValues(rows, GOODS_GROUP2.getName());

        for(String group: groupValues){
            BigDecimal invoiceSumInGroup = ZERO;
            BigDecimal marginInGroup = ZERO;
            BigDecimal marginWithoutVATInGroup = ZERO;

            for(ReportRow row: rows){
                if(row.getGoodsGroup2().equals(group)){
                    invoiceSumInGroup = invoiceSumInGroup.add(row.getRowSum());
                    marginInGroup = marginInGroup.add(row.getMargin());
                    marginWithoutVATInGroup = marginWithoutVATInGroup.add(row.getMarginWithoutVAT());
                }
            }
            putToProfitabilityMap(profitabilityMap, group, invoiceSumInGroup, marginInGroup);
            putToProfitabilityMap(profitabilityWithoutVATMap, group, invoiceSumInGroup, marginWithoutVATInGroup);
        }

        if(groupingOrder == 1){
            rows.forEach(row -> row.setProfitabilityByGroup1(
                    profitabilityMap.get(row.getGoodsGroup2())));
            rows.forEach(row -> row.setProfitabilityWithoutVATByGroup1(
                    profitabilityWithoutVATMap.get(row.getGoodsGroup2())));
        }
        if(groupingOrder == 2){
            rows.forEach(row -> row.setProfitabilityByGroup2(
                    profitabilityMap.get(row.getGoodsGroup2())));
            rows.forEach(row -> row.setProfitabilityWithoutVATByGroup2(
                    profitabilityWithoutVATMap.get(row.getGoodsGroup2())));
        }
    }

    void setProfitabilityByGroups(List<ReportRow> rows){
        int groupingOrder = 0;

        for(String groupName: settings.getGroupings()){
            groupingOrder++;

            if(groupName.equals(GOODS_GROUP2.getName())){
                if(groupingOrder == 1){
                    setGoodsGroup2Profitability(rows, groupingOrder);
                } else{
                    setGoodsGroup2Profitability(rows,
                            settings.getGroupings().get(groupingOrder - 2),
                            groupingOrder);
                }
            }
            if(groupName.equals(CLIENT_NAME.getName())){
                if(groupingOrder == 1){
                    setClientProfitability(rows, groupingOrder);
                } else{
                    setClientProfitability(rows,
                            settings.getGroupings().get(groupingOrder - 2),
                            groupingOrder);
                }
            }
            if(groupName.equals(INVOICE_NUMBER.getName())){
                if(groupingOrder == 1){
                    setInvoiceNumProfitability(rows, groupingOrder);
                } else{
                    setInvoiceNumProfitability(rows,
                            settings.getGroupings().get(groupingOrder - 2),
                            groupingOrder);
                }
            }
        }
    }
}
