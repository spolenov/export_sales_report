package com.century.report;

import java.io.Serializable;
import java.util.List;

public class ReportRequest<T> implements Serializable {
    ReportSettings settings;
    List<T> data;
}
