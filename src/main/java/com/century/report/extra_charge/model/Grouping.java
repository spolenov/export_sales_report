package com.century.report.extra_charge.model;

import lombok.Getter;

@Getter
public enum Grouping {
    CLIENT_NAME("clientName"),
    DATE_DOC("dateDoc"),
    INVOICE_NUMBER("invoiceNumber"),
    GOODS_GROUP2("goodsGroup2"),
    NULL("null");

    String name;

    Grouping(String name){
        this.name = name;
    }
}
