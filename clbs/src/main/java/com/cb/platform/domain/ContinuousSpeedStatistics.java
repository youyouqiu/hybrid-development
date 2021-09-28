package com.cb.platform.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 严重程度阶段统计实体
 * @author hujun
 * @date 2018/5/413:39
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ContinuousSpeedStatistics implements Serializable{
    private static final long serialVersionUID = 1L;

    private int shortForGeneral = 0;//超速一般严重5分钟以下
    private int middleForGeneral = 0;//超速一般严重5-10分钟
    private int longForGeneral = 0;//超速一般严重10分钟以上

    private int shortForRelatively = 0;//超速比较严重5分钟以下
    private int middleForRelatively = 0;//超速比较严重5-10分钟
    private int longForRelatively = 0;//超速比较严重10分钟以上

    private int shortForEspecially = 0;//超速特别严重5分钟以下
    private int middleForEspecially = 0;//超速特别严重5-10分钟
    private int longForEspecially = 0;//超速特别严重10分钟以上

    private int total = 0;//合计
}
