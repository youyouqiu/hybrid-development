package com.zw.platform.domain.functionconfig.form;

import java.io.Serializable;

import com.zw.platform.util.common.BaseFormBean;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GpsLine extends BaseFormBean implements Serializable {

    private String name; // 行驶路线名称

    private String type; // 路线类型

    private Integer excursion; // 路线偏移量

    private String description; // 描述

    private String startToEndLng;

    private String startToEndLat;

    private Double startLongitude; // 开始位置经度

    private Double startLatitude; // 开始位置纬度

    private Double endLongitude; // 结束位置经度

    private Double endLatitude; // 结束位置纬度

    private String groupId; // 所属企业

    /**
     * 新增或者修改路线标识：0-新增，1-修改
     */
    private String addOrUpdateTravelFlag = "0";

    /**
     * 被修改行驶路线的id
     */
    private String travelLineId = "";

    /**
     * 途经点经度集合-和相应的纬度一一对应(点1,点2,点3,点4......)
     */
    private String wayPointLng;

    /**
     * 途经点纬度集合-和相应的经度一一对应(点1,点2,点3,点4......)
     */
    private String wayPointLat;

    /**
     * 所有点经度集合-和相应的纬度一一对应(点1,点2,点3,点4......)
     */
    private String allPointLng;

    /**
     * 所有点纬度集合-和相应的经度一一对应(点1,点2,点3,点4......)
     */
    private String allPointLat;
}
