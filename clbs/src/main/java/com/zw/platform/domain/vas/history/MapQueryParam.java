package com.zw.platform.domain.vas.history;

import lombok.Data;

/**
 * Created by LiaoYuecai on 2017/4/12.
 */
@Data
public class MapQueryParam {
    private String leftTopLongitude;//东北角经度
    private String leftTopLatitude;//东北角纬度
    private String rightFloorLongitude;//西南角经度
    private String rightFloorLatitude;//西南角纬度
    private String vehicleId;//车辆ID
    private Long startTime;//开始时间
    private Long endTime;//结束时间
    /**
     * 开始条数
     */
    private int startSize = 0;
    /**
     * 查询条数
     */
    private int querySize;

    private Integer reissue;
}
