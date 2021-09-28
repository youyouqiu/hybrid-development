package com.zw.platform.domain.basicinfo.query;

import lombok.Data;

import java.io.Serializable;

/**
 * 监控对象使用的图标文件名实体
 * @author hujun
 * @date 2018/6/28 15:23
 */
@Data
public class UseIco implements Serializable{
    private static final long serialVersionUID = 1L;
    private String monitorId; //车辆id
    private String monitorCategoryIcoName; //车辆类别图标文件名
    private String monitorSubTypeIcoName; //车辆子类型图标文件名
    private String monitorIcoName; //车辆图标文件名
}
