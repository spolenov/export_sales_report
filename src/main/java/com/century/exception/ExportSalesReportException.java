package com.century.exception;

import lombok.Getter;

public class ExportSalesReportException extends RuntimeException{
    @Getter
    private final String message;

    public ExportSalesReportException(String message){
        super(message);
        this.message = message;
    }
}
