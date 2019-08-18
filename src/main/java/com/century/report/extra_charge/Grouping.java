package com.century.report.extra_charge;

import lombok.Getter;

@Getter
public enum Grouping {
    CLIENT_NAME("clientName"),
    INVOICE_NUMBER("invoiceNumber"),
    GOODS_GROUP2("goodsGroup2");

    String name;

    Grouping(String name){
        this.name = name;
    }
}
