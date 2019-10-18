package com.century.report.service;

import com.century.report.ReportName;
import com.century.report.ReportRequest;
import com.century.report.ReportType;
import com.century.report.StringResult;
import com.century.report.extra_charge.model.Invoice;

public interface ReportService {
    StringResult doReport(ReportName reportName, ReportType type, ReportRequest<Invoice> request);
}
