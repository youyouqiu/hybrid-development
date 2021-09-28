package com.zw.platform.domain.multimedia;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by wangying on 2017/4/1.
 */
@Data
public class Media implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id; // 数据库id
    private Integer type;//多媒体类型
    private Integer formatCode;//多媒体格式编码
    private Integer eventCode;//事件项编码
    private Integer wayID;//通道ID
    private String vehicleId;//车辆ID
    private String brand; // 车牌号
    private String assignment; // 分组
    private String mediaName; // 名称
    private String mediaUrl; // 存储路径
    private Integer flag;
    private Date createDataTime;
    private String createDataUsername;
    private String startTime;
    private String endTime;
    private String mediaUrlNew;
    private Integer plateColor; // 车牌颜色
    private String description; // 描述

    /**
     * 多媒体上传时间
     */
    private Date uploadTime;
}
