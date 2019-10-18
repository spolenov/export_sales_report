package com.century.web;

import com.century.report.ReportRequest;
import com.century.report.StringResult;
import com.century.report.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.century.report.ReportName.EXTRA_CHARGE;
import static com.century.report.ReportType.EXCEL;

@Slf4j
@RestController
@RequestMapping("/")
public class ExtraChargeReportController extends AbstractController{
    @Override
    String getClassName() {
        return this.getClass().getSimpleName();
    }

    @Autowired
    ReportService reportService;

    //Сохранить накладные в БД
    @PostMapping(value = "/extra_charge", produces ="application/json")
    @ResponseBody
    public StringResult saveInvoices(@RequestBody ReportRequest request){
        log.info("Received extra charge report settings, invoice count = {}...", request.getData().size());
        return reportService.doReport(EXTRA_CHARGE, EXCEL, request);
    }
}
