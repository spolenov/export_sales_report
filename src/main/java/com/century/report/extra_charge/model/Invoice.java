package com.century.report.extra_charge.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Invoice {
    private Date dateDoc;
    private BigDecimal summa;
    private Client client;
    private String invoiceNumber;

    private List<InvoiceRow> rows;

    public InvoiceRow getRow(int index){
        return this.rows.stream()
                .filter(b -> b.getRowNum() == index)
                .findAny()
                .orElse(null);
    }

    public List<ReportRow> getReportRows(){
        return this.rows.stream()
                .map(r -> new ReportRow(this, r))
                .collect(Collectors.toList());
    }
}
