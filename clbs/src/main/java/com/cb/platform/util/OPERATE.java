package com.cb.platform.util;

public enum OPERATE {

    ADD("新增"), UPDATE("修改"),

    DELETE("删除"), DELETEBATCH("批量删除"),

    IMPORT("导入"), EXPORT("导出"), RELATION_DELETE("关联删除");

    private String name;

    private OPERATE(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
