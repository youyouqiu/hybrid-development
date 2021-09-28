package com.zw.lkyw.domain;

import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/12/30 14:10
 @Description 报表菜单类
 @version 1.0
 **/
@Data
public class ReportMenu {
    private String name;
    private String url;
    private transient String id;

    public static ReportMenu getInstance(String name) {
        ReportMenu reportMenu = new ReportMenu();
        reportMenu.name = name;
        return reportMenu;

    }
}
