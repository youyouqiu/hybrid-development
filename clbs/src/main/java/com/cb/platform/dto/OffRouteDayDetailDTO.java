package com.cb.platform.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/3/24 10:22
 */
@Data
public class OffRouteDayDetailDTO implements Serializable {
    private static final long serialVersionUID = -6878555688291453919L;
    /**
     * 日期(格式:yyyyMMdd)
     */
    private String day;
    /**
     * 路线偏离报警数
     */
    private Integer courseDeviation;
    /**
     * 不按规定线路行驶报警数
     */
    private Integer notFollowLine;
    /**
     * 同比(比上月)
     */
    private String monthRatio;
    /**
     * 环比
     */
    private String ringRatio;
}
