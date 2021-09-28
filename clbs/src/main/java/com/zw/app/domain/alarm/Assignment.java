package com.zw.app.domain.alarm;

import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class Assignment {

    private String assId;

    private String assName;

    private int total;

    private List<Monitor> monitors;

    private transient List<String> mids;

    private transient Map<String,Monitor> assApp;
}
