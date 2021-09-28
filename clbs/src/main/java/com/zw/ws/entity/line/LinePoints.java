
package com.zw.ws.entity.line;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * <p>
 * Title: LinePoints.java
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 *
 * @version 1.0
 * @author: Jiangxiaoqiang
 * @date 2016年9月6日上午10:30:29
 */
@Data
public class LinePoints implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer linePointId;

    private Integer lineId; // 线的坐标

    private Integer sortOrder; // 顺序

    private Double longitude; // 经度

    private Double latitude; // 纬度

    private Integer flag; // 逻辑删除标志

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    private Integer width;// 路线宽度

    private Long attribute;// 路线属性

    private Integer runTimeMax;// 行驶过长阈值

    private Integer runTimeMin;// 行驶不足阈值

    private Integer maxSpeed;// 最高速度

    private Integer overSpeedLastTime;// 超速持续时间

    /**
     * 夜间最大速度（2019版本协议专有）
     */
    private Integer nightMaxSpeed;
}
