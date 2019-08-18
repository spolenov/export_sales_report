package com.century.report.generator;

import java.util.List;
import java.util.Map;

public class JasperEntity {
    private String reportName;
    private final Map<String, Object> params;
    private final List<Map<String, ?>> fields;

    public JasperEntity(Map<String, Object> params, List<Map<String, ?>> fields, String reportName) {
        this.params = params;
        this.fields = fields;
        this.reportName = reportName;
    }

    /**
     * Парметры для JasperReports
     */
    public Map<String, Object> jasperParams() {
        return params;
    }

    /**
     * Поля для формирования отчёта
     */
    public List<Map<String, ? extends Object>> jasperFields() {
        return fields;
    }

    /**
     * Имя файла *.jasper
     */
    public String reportName() {
        return reportName;
    }
}
