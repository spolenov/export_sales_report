package com.century.report.extra_charge;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;

@Data
@NoArgsConstructor
public class ReportRow {
    private String dateDoc;
    private BigDecimal summa;
    private Integer clientId;
    private String clientName;

    private String invoiceNumber;

    private Integer rowNum;

    private String goodsName;
    private String goodsGroup2;

    private BigDecimal qty;
    private BigDecimal incomePrice;
    private BigDecimal incomePriceWithoutVAT;

    private BigDecimal expenditurePrice;
    private BigDecimal extraCharge1C;
    private BigDecimal extraChargeExport;
    private BigDecimal rowSum;

    public ReportRow(Invoice invoice, InvoiceRow row){
        this.dateDoc = new SimpleDateFormat("dd.MM.yyyy").format(invoice.getDateDoc());
        this.summa = invoice.getSumma();
        this.clientId = invoice.getClient().getId();
        this.clientName = invoice.getClient().getName();
        this.invoiceNumber = invoice.getInvoiceNumber();

        this.rowNum = row.getRowNum();
        this.goodsName = row.getGoodsName();
        this.qty = row.getQty();
        this.incomePrice = row.getIncomePrice();

        this.incomePriceWithoutVAT = this.incomePrice.divide(
                new BigDecimal(row.vat).divide(new BigDecimal(100), RoundingMode.HALF_UP).add(BigDecimal.ONE), RoundingMode.HALF_UP);

        this.expenditurePrice = row.getExpenditurePrice();
        this.extraCharge1C = row.getExtraCharge();
        this.rowSum = row.getRowSum();
        this.goodsGroup2 = row.getGoodsGroup2();
    }
}
