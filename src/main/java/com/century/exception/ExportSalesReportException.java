package com.century.exception;

import lombok.Getter;

public class ExportSalesReportException extends RuntimeException{
    @Getter
    private final String message;

    public ExportSalesReportException(String message){
        super(message);
        this.message = message;
    }

    public ExportSalesReportException(Exception e){
        super(e);
        this.message = e.getMessage();
    }

    public ExportSalesReportException(String message, Exception e){
        super(message, e);
        this.message = message;
    }
}
