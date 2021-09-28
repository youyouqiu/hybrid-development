package com.zw.platform.dto.protocol;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/9/27 9:10
 */
@Data
public class GeneralVehicleDto {

    /**
     * 车牌号
     */
    private String vin;

    /**
     * 车牌颜色
     */
    private String vehicleColor = "2";

    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 运输行业编码
     */
    private String transType;

    /**
     * 车籍地
     */
    private String vehicleNationality;
    /**
     * 经营范围代码
     */
    private String businessScopeCode;

    /**
     * 业户ID
     */
    private String owersId;

    /**
     * 业户名称
     */
    private String owersName;

    /**
     * 业户联系电话
     */
    private String owersTel;

    public String toJt2019String() {
        return "VIN:=" + valueToString(vin)
            + ";VEHICLE_COLOR:=" + valueToString(vehicleColor)
            + ";VEHICLE_TYPE:=" + valueToString(vehicleType)
            + ";TRANS_TYPE:=" + valueToString(transType)
            + ";VEHICLE_NATIONALITY:=" + valueToString(vehicleNationality)
            + ";BUSINESSSCOPECODE:=" + valueToString(businessScopeCode)
            + ";OWERS_ID:=" + valueToString(owersId)
            + ";OWERS_NAME:=" + valueToString(owersName)
            + ";OWERS_TEL:=" + valueToString(owersTel);
    }

    public String toJt2013String() {
        return "VIN:=" + valueToString(vin)
            + ";VEHICLE_COLOR:=" + valueToString(vehicleColor)
            + ";VEHICLE_TYPE:=" + valueToString(vehicleType)
            + ";TRANS_TYPE:=" + valueToString(transType)
            + ";VEHICLE_NATIONALITY:=" + valueToString(vehicleNationality)
            + ";OWERS_ID:=" + valueToString(owersId)
            + ";OWERS_NAME:=" + valueToString(owersName)
            + ";OWERS_TEL:=" + valueToString(owersTel);
    }

    private String valueToString(String value) {
        return StringUtils.isBlank(value) ? "" : value;
    }
}
