package com.zw.platform.util.excel;

import lombok.Data;

import java.io.OutputStream;
import java.util.List;

@Data
public class ExportExcelParam {
    private String title;// 导出excel的标题

    private int type;// 字段类型（0：导出导入；1：仅导出；2：仅导入）

    private List<?> exportData;// 要导出的数据

    private Class<?> entityClass;// 要到导出的class

    private int[] group; // 字段归属组（根据分组导出导入）

    private OutputStream out;// excel要写到的目的地方

    private List<String> customColumnList;

    public ExportExcelParam(String title, int type, List<?> exportData, Class<?> entityClass, int[] group,
        OutputStream out) {
        this.title = title;
        this.type = type;
        this.exportData = exportData;
        this.entityClass = entityClass;
        this.group = group;
        this.out = out;
    }

    public ExportExcelParam(String title, int type, List<?> exportData, Class<?> entityClass, int[] group,
        OutputStream out, List<String> customColumnList) {
        this.title = title;
        this.type = type;
        this.exportData = exportData;
        this.entityClass = entityClass;
        this.group = group;
        this.out = out;
        this.customColumnList = customColumnList;
    }
}
