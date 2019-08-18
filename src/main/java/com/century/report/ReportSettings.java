package com.century.report;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;

@Data
public class ReportSettings implements Serializable {
    private String username;
    private String filename;
    private String programName;
    private Date startDate;
    private Date endDate;
    private boolean detailedByDataElements;
    private LinkedList<String> groupings;
}
