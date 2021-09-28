package com.zw.platform.domain.basicinfo.query;

import com.zw.platform.basic.dto.query.VehiclePageQuery;
import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 车辆Query
 * @author wangying
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 车辆信息
     */
    private String id;

    /**
     * 车辆编号
     */
    private String vehicleNumber;

    /**
     * 车主
     */
    private String vehicleOwner;

    /**
     * 车主电话
     */
    private String vehicleOwnerPhone;

    /**
     * 别名
     */
    private String aliases;

    /**
     * 车辆类型
     */
    private String vehicleType;

    private String vehiclet;

    /**
     * 机架号
     */
    private String chassisNumber;

    /**
     * 发动机号
     */
    private String engineNumber;

    /**
     * 车牌号
     */
    private String brand;

    /**
     * 区域属性
     */
    private String areaAttribute;

    /**
     * 省、直辖市
     */
    private String province;

    /**
     * 市、区
     */
    private String city;

    /**
     * 燃油类型
     */
    private String fuelType;

    /**
     * 车牌颜色（0蓝、1黄、2白、3黑）
     */
    private Integer plateColor;

    /**
     * 0不显示、1显示
     */
    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    /**
     * 组织查询参数（分组或者企业）
     */
    private String groupName;

    /**
     * 组织类型
     */
    private String groupType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 用于判断前端传递的是运输证还还是行驶证， 0: 全部;1:行驶证;2:运输证;3:行驶证和运输证
     */
    private Integer tipType = 0;
    /**
     * 行驶证查询类型 0:全部; 1:即将到期; 2:已到期
     */
    private Integer drivingLicenseType = 0;
    /**
     * 运输证查询类型 0:全部; 1:即将到期; 2:已到期
     */
    private Integer roadTransportType = 0;
    /**
     * 车辆保养车型类型 0:全部; 2:已到期
     */
    private Integer maintenanceType = 0;

    public VehiclePageQuery convert() {
        VehiclePageQuery query = new VehiclePageQuery();
        query.setDraw(this.getDraw());
        query.setQueryType(this.getQueryType());
        query.setStart(this.getStart());
        query.setLength(this.getLength());
        query.setLimit(this.getLimit());
        query.setEnd(this.getEnd());
        query.setPage(this.getPage());
        query.setSimpleQueryParam(this.getSimpleQueryParam());
        query.setDrivingLicenseType(this.drivingLicenseType);
        query.setRoadTransportType(this.roadTransportType);
        query.setMaintenanceType(this.maintenanceType);
        return query;
    }

}
