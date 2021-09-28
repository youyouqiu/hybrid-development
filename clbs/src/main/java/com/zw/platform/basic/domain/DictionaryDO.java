package com.zw.platform.basic.domain;

import lombok.Data;

/**
 * zw_c_dictionary
 *
 * @author zhangjuan
 * @date 2020/10/20
 */
@Data
public class DictionaryDO {
    /**
     * 字典表主键id
     */
    private String id;

    /**
     * 父id
     */
    private String pid;

    /**
     * 字典的唯一标识
     */
    private String code;

    /**
     * 值
     */
    private String value;

    /**
     * 字典类型
     */
    private String type;

    /**
     * 描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;
}
