package com.zw.platform.util.excel;

import lombok.Data;

import java.io.OutputStream;
import java.util.List;

@Data
public class BigDataExportExcelParam {

    private int windowSize;//导出excel的窗口的row行数

    private String title;// 导出excel的标题

    private int type;// 字段类型（0：导出导入；1：仅导出；2：仅导入）

    private List<?> exportData;// 要导出的数据

    private Class<?> entityClass;// 要到导出的class

    private int[] group; // 字段归属组（根据分组导出导入）

    private OutputStream out;// excel要写到的目的地方

    public BigDataExportExcelParam(int windowSize, String title, int type, List<?> exportData,
                                   Class<?> entityClass, int[] group,
                            OutputStream out) {
        this.windowSize = windowSize;
        this.title = title;
        this.type = type;
        this.exportData = exportData;
        this.entityClass = entityClass;
        this.group = group;
        this.out = out;
    }

}
