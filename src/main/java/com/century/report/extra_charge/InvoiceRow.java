package com.century.report.extra_charge;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceRow {
    int rowNum;
    Integer goodsId;
    String goodsName;

    BigDecimal qty;
    BigDecimal incomePrice;
    BigDecimal expenditurePrice;
    BigDecimal extraCharge;
    BigDecimal rowSum;

    Integer vat;
}
