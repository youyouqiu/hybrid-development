package com.zw.platform.domain.statistic;

import lombok.Data;

import java.io.Serializable;

/**
 * 字典表
 * @author zhouzongbo on 2019/1/2 13:56
 */
@Data
public class DictionaryInfo implements Serializable {
    private static final long serialVersionUID = 4256998346040327393L;

    private String id;

    private String pid;
    private String code;

    private String value;

    private String type;

    private String description;
}
