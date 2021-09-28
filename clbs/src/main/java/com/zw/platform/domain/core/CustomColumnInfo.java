package com.zw.platform.domain.core;

import lombok.Data;

import java.io.Serializable;

/**
 * 定制列初始化数据
 * @author zhouzongbo on 2019/3/11 14:17
 */
@Data
public class CustomColumnInfo implements Serializable {

    private static final long serialVersionUID = 478142390498374236L;

    private String id;

    /**
     * 页面显示名称
     */
    private String title;

    /**
     * 字段ID
     */
    private String name;

    /**
     * 所属功能标识
     */
    private String mark;
}
