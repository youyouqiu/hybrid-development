package com.zw.platform.domain.riskManagement;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zjc on 2017/8/16.
 */
@Data
public class RiskEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 风险事件信息
     */
    private String id;

    /**
     * 风险事件
     */
    private String riskEvent;

    /**
     * 风险类型
     */
    private String riskType;

    /**
     * 风险描述
     */
    private String description;

    /**
     * 功能id
     */
    private int functionId;

    private Short flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    /**
     * 报警事件中文统称
     */
    private String eventCommonName;

    /**
     * 报警事件英文统称
     */
    private String eventCommonFiled;

    /**
     * 逗号分隔的functionId拼接
     */
    private String functionIds;

    /**
     * 风险类型的数字形式
     */
    private Integer riskTypeNum;
  
}
