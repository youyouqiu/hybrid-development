package com.zw.platform.domain.reportManagement.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/***
 @Author zhengjc
 @Date 2019/4/30 15:45
 @Description 车辆综合信息查询实体
 @version 1.0
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class VehGeneralInfoQuery extends BaseQueryBean {

    /**
     * 车辆id
     */
    private String vehicleIds;

    /**
     * 车辆类型ID
     */
    private String vehType;

    /**
     * 车牌号
     */
    private String brand;

    /**
     * SIM卡
     */
    private String simCard;

    /**
     * 设备编号
     */
    private String deviceNumber;

    /**
     * 从业人员
     */
    private String professional;
}
