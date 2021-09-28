package com.zw.platform.domain.vas.history;

import lombok.Data;

import java.io.Serializable;

/**
 * 区域DTO
 * @author zhouzongbo on 2019/5/5 19:38
 */
@Data
public class AreaInfo implements Serializable {

    private static final long serialVersionUID = -1199221249924900391L;

    /**
     * 东北角经度
     */
    private Double leftTopLongitude;

    /**
     * 东北角纬度
     */
    private Double leftTopLatitude;

    /**
     * 西南角经度
     */
    private Double rightFloorLongitude;

    /**
     * 西南角纬度
     */
    private Double rightFloorLatitude;

    /**
     * 区域名称(后续可能会删除该字段，原有轨迹回放使用该字段)
     */
    private String areaName;
    /**
     * 区域名称(为兼容paas_cloud接口添加,新做的轨迹回访使用该字段)
     */
    private String areaId;
}
