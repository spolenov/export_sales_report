package com.century.report.extra_charge.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExtraChargeUtilService {
    @Value("${excel.dir.local}")
    private String excelDirLocal;

    @Value("${max.grouping.count}")
    private int maxGroupingCount;

    @Value("${bigdecimal.scale}")
    private int bigDecimalScale;

    String getExcelFileFullPath(String fileName){
        if(excelDirLocal == null || excelDirLocal.isEmpty()){
            return String.format("%s.xls", fileName);
        }
        return String.format("%s\\%s.xls",
                excelDirLocal, fileName);
    }

    int getScale(){
        return bigDecimalScale;
    }

    int getMaxGroupingCount(){
        return maxGroupingCount;
    }
}
