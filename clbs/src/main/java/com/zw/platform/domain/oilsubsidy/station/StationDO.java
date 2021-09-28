package com.zw.platform.domain.oilsubsidy.station;

import lombok.Data;

import java.util.Date;

/**
 * 公交线路站点信息表
 *
 * @author zhangjuan 2020-10-09
 */
@Data
public class StationDO {
    /**
     * 公交线路站点
     */
    private String id;
    /**
     * 站点名称
     */
    private String name;
    /**
     * 站点编号
     */
    private String number;
    /**
     * 站点经度
     */
    private Double longitude;
    /**
     * 站点纬度
     */
    private Double latitude;
    /**
     * 站点描述
     */
    private String describe;
    /**
     * 备注
     */
    private String remark;
    /**
     * 逻辑删除标记
     */
    private Integer flag;
    /**
     * 创建时间
     */
    private Date createDataTime;
    /**
     * 创建用户
     */
    private String createDataUsername;
    /**
     * 更新时间
     */
    private Date updateDataTime;
    /**
     * 更新用户
     */
    private String updateDataUsername;
}
