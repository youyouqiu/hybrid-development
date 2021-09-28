/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential
 * and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you
 * entered into with ZhongWei.
 */

package com.zw.ws.entity.t808.location.defence;

import lombok.Data;

import java.io.Serializable;


/**
 * <p>
 * Title: RectangleAreaItem.java
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
 * @date 2016年9月7日上午9:09:37
 */
@Data
public class RectangleAreaItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /// <summary>
    /// 区域ID
    /// </summary>
    private Integer rectangleAreaId;

    /// <summary>
    /// 区域属性
    /// </summary>
    private Long rectangleAreaProperty;

    /// <summary>
    /// 左上点纬度,以度为单位的纬度值乘以10的6次方，精确到百万分之一度
    /// </summary>
    private Double leftTopLatitude;

    /// <summary>
    /// 右下點经度,以度为单位的经度值乘以10的6次方，精确到百万分之一度
    /// </summary>
    private Double rightBottomLongitude;

    /// <summary>
    /// 右下点纬度,以度为单位的纬度值乘以10的6次方，精确到百万分之一度
    /// </summary>
    private Double rightBottomLatitude;

    /// <summary>
    /// 左上點经度,以度为单位的经度值乘以10的6次方，精确到百万分之一度
    /// </summary>
    private Double leftTopLongitude;

    /// <summary>
    /// 起始时间,YY-MM-DD-hh-mm-ss，若区域属性0位为0则没有该字段
    /// </summary>
    private String startTime;

    /// <summary>
    /// 结束时间,YY-MM-DD-hh-mm-ss，若区域属性0位为0则没有该字段
    /// </summary>
    private String endTime;

    /// <summary>
    /// 最高速度,Km/h，若区域属性1位为0则没有该字段
    /// </summary>
    private Integer maxSpeed;

    /// <summary>
    /// 超速持续时间,单位秒(s),若区域属性1位为0则没有该字段
    /// </summary>
    private Integer overSpeedLastTime;

    /// <summary>
    /// 拍照启动最高速度,单位为公里每小时(km/h)，当速度降到最高速度以下就启动拍照,若区域属性8位为0则没有该字段
    /// </summary>
    private Integer photoMaxSpeed;

    /// <summary>
    /// 速度降到最高速度以下继续时间,单位为秒(S),若区域属性8位为0则没有该字段
    /// </summary>
    private Integer lastTimeBelowPhotoMaxSpeed;

    /// <summary>
    /// 点火拍照时间间隔,单位5秒，如果为0关闭点火拍照,若区域属性8位为0则没有该字段
    /// </summary>
    private Integer fireOnPhotoInterval;

    /// <summary>
    /// 熄火拍照延时时间,单位分钟，若区域属性8位为0则没有
    /// </summary>
    private Integer fireOffPhotoDelay;

    /// <summary>
    /// 区域名称长度,若区域属性15位为0 则没有该字段
    /// </summary>
    private Integer rectangleAreaNameLength;

    /// <summary>
    /// 区域名称,经GBK编码 若区域属性15位为0 则没有改字段
    /// </summary>
    private String rectangleAreaName;

    /**
     * 夜间最大速度（2019版本协议专有）
     */
    private Integer nightMaxSpeed;

    /**
     * 2019协议端参数改变
     */
    private Integer areaId;

    /**
     * 2019协议端参数改变
     */
    private Long areaProperty;

    /**
     * 2019协议端参数改变
     */
    private Integer areaNameLen;

    /**
     * 2019协议端参数改变
     */
    private String areaName;

    public void initParam2019() {
        areaId = rectangleAreaId;
        areaProperty = rectangleAreaProperty;
        areaName = rectangleAreaName;
        areaNameLen = rectangleAreaNameLength;

    }
}
