package com.zw.platform.domain.reportManagement;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/3/8.
 */
@Data
public class BigDataReport implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String count;
    private String longtitude;
    private String latitude;}