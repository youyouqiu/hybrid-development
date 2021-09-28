package com.zw.talkback.util.excel;

import lombok.Data;

import java.io.OutputStream;
import java.util.List;

/**
 * 功能描述: 导出参数初始化
 */
@Data
public class ExportExcelParam {
    /**
     * 导出excel的标题
     */
    private String title;
    /**
     * 字段类型（0：导出导入；1：仅导出；2：仅导入）
     */
    private int type;
    /**
     * 要导出的数据
     */
    private List<?> exportData;
    /**
     * 要到导出的class
     */
    private Class<?> entityClass;
    /**
     * 字段归属组（根据分组导出导入）
     */
    private int[] group;
    /**
     * excel要写到的目的地方
     */

    private OutputStream out;

    public ExportExcelParam(String title, int type, List<?> exportData, Class<?> entityClass, int[] group,
        OutputStream out) {
        this.title = title;
        this.type = type;
        this.exportData = exportData;
        this.entityClass = entityClass;
        this.group = group;
        this.out = out;
    }

    public ExportExcelParam(List<?> exportData, Class<?> entityClass, OutputStream out) {
        this(null, 1, exportData, entityClass, null, out);
    }

}
