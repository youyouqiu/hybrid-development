/*
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.ws.entity.t808.location.defence;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class T808Msg0x8604 implements T808MsgBody {
    private static final long serialVersionUID = 1L;

    /// <summary>
    /// 区域ID
    /// </summary>
    private Integer areaId;

    /// <summary>
    /// 区域属性
    /// </summary>
    private Long areaParam;

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
    private Integer overSpeedTime;

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
    /// 区域总顶点数
    /// </summary>
    private Integer topSum;

    private Integer packageVertexCount; // 包顶点数

    /// <summary>
    /// 区域名称长度,若区域属性15位为0 则没有该字段
    /// </summary>
    private Integer polygonAreaNameLength;

    /// <summary>
    /// 区域名称,经GBK编码 若区域属性15位为0 则没有改字段
    /// </summary>
    private String polygonAreaName;

    /// <summary>
    /// 顶点项
    /// </summary>
    private List<PolygonNodeItem> topItems = new ArrayList<>();

    /**
     * 夜间最大速度（2019版本协议专有）
     */
    private Integer nightMaxSpeed;

    /**
     * 2019协议端参数改变
     */
    private String areaName;

    public void initParam2019() {

        areaName = polygonAreaName;

    }

}
