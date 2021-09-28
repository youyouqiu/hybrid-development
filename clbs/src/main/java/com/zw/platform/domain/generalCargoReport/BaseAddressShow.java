package com.zw.platform.domain.generalCargoReport;

import lombok.Data;

import java.io.Serializable;

/***
 @Author zhengjc
 @Date 2019/9/5 17:40
 @Description 地址基础信息类
 @version 1.0
 **/
@Data
public abstract class BaseAddressShow implements Serializable {
    protected String address = "";
    protected String alarmStartLocation;

    public abstract void initExportData();
}
