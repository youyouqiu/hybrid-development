package com.cb.platform.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 持续超速道路运输企业统计返回实体
 * @author hujun
 * @Date 创建时间：2018年4月27日 上午10:23:37
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ContinuousSpeedGroupStatistics implements Serializable {
    private static final long serialVersionUID = 8875667252806086481L;

    /**
     * 道路运输企业
     */
    private String orgName;
    /**
     * 超速一般严重5分钟以下
     */
    private int shortForGeneral = 0;
    /**
     * 超速一般严重5-10分钟
     */
    private int middleForGeneral = 0;
    /**
     * 超速一般严重10分钟以上
     */
    private int longForGeneral = 0;
    /**
     * 超速比较严重5分钟以下
     */
    private int shortForRelatively = 0;
    /**
     * 超速比较严重5-10分钟
     */
    private int middleForRelatively = 0;
    /**
     * 超速比较严重10分钟以上
     */
    private int longForRelatively = 0;
    /**
     * 超速特别严重5分钟以下
     */
    private int shortForEspecially = 0;
    /**
     * 超速特别严重5-10分钟
     */
    private int middleForEspecially = 0;
    /**
     * 超速特别严重10分钟以上
     */
    private int longForEspecially = 0;
    /**
     * 合计
     */
    private int total = 0;
}
