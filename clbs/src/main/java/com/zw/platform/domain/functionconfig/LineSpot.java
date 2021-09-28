package com.zw.platform.domain.functionconfig;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 关键点
 *
 * @author  Tdz
 * @create 2017-04-01 16:06
 **/
@Data
public class LineSpot implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 线上打点
     */
    private String id;
    /**
     * 线id
     */
    private String lineId;

    /**
     * 名称
     */
    private String name;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 到达时间
     */
    private Date arriveTime;

    /**
     * 离开时间
     */
    private Date leaveTime;

    /**
     * 描述
     */
    private String description;
    private Integer flag; // 逻辑删除标志
    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;
}
