package com.century.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class ReportRequest<T> implements Serializable {
    @Getter
    ReportSettings settings;
    @Getter
    List<T> data;

    public <K> List<K> getDataAs(Class<K> clazz){
        return data.stream().map(d -> getElementAs(d, clazz))
                .collect(Collectors.toList());
    }

    private <K> K getElementAs(T element, Class<K> clazz){
        return new ObjectMapper().convertValue(element, clazz);
    }
}
