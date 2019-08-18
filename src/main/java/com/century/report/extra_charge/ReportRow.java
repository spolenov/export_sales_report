package com.century.report.extra_charge;

import lombok.Data;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

@Data
public class ReportRow {
    private String dateDoc;
    private BigDecimal summa;
    private Integer clientId;
    private String clientName;

    private String invoiceNumber;

    private Integer rowNum;
    private String goodsName;
    private BigDecimal qty;
    private BigDecimal incomePrice;
    private BigDecimal expenditurePrice;
    private BigDecimal extraCharge;

    public ReportRow(Invoice invoice, InvoiceRow row){
        this.dateDoc = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(invoice.getDateDoc());
        this.summa = invoice.getSumma();
        this.clientId = invoice.getClient().getId();
        this.clientName = invoice.getClient().getName();
        this.invoiceNumber = invoice.getInvoiceNumber();

        this.rowNum = row.getRowNum();
        this.goodsName = row.getGoodsName();
        this.qty = row.getQty();
        this.incomePrice = row.getIncomePrice();
        this.expenditurePrice = row.getExpenditurePrice();
        this.extraCharge = row.getExtraCharge();
    }
}
