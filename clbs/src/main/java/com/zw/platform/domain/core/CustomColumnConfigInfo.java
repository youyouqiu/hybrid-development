package com.zw.platform.domain.core;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户定制列
 * @author zhouzongbo on 2019/3/11 14:22
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CustomColumnConfigInfo extends BaseFormBean {
    private static final long serialVersionUID = -2057759705504931682L;

    /**
     * 默认列
     */
    public static final int DEFAULT_COLUMN = 0;
    /**
     * 非默认列
     */
    public static final int NOT_DEFAULT_COLUMN = 1;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否固定列: 0::不固定, 1:固定
     */
    private Integer isFix;

    /**
     * 显示字段
     */
    private String title;

    /**
     * 初始列列名
     */
    private String columnName;

    /**
     * 初始化列ID
     */
    private String columnId;

    /**
     * 初始列功能标识
     */
    private String mark;

    /**
     * 初始列列信息: 0：默认列;  1:非默认列 ; 9: 固定列
     */
    private Integer status;

    /**
     * 初始值
     */
    private String initValue;
}
