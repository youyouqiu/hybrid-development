package com.zw.platform.dto.protocol;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 客运车辆静态信息数据
 * @author penghj
 * @version 1.0
 * @date 2020/9/25 10:34
 */
@Data
public class PassengerVehicleDto {
    /**
     * 运输行业编码
     */
    private String transType;
    /**
     * 车牌号
     */
    private String vin;

    /**
     * 车籍地
     */
    private String vehicleNationality;
    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 道路运输证号
     */
    private String rtpn;
    /**
     * 运营商
     */
    private String operatorName;
    /**
     * 运营商联系电话
     */
    private String operatorTel;
    /**
     * 业户名称
     */
    private String owersName;
    /**
     * 业户原ID
     */
    private String owersOrigId;

    /**
     * 业户联系电话
     */
    private String owersTel;
    /**
     * 经营许可证号
     */
    private String rtoln;
    /**
     * 车辆厂牌型号
     */
    private String vehicleMode;
    /**
     * 车牌颜色
     */
    private String vehicleColor;
    /**
     * 车辆原编号
     */
    private String vehicleOrigId;
    /**
     * 驾驶员情况
     */
    private String driverInfo;

    /**
     * 经营区域
     */
    private String businessArea;
    /**
     * 经营代码范围
     */
    private String businessScopeCode;
    /**
     * 班线类型
     */
    private String banLineType;
    /**
     * 核定座位
     */
    private String approvedSeats;
    /**
     * 始发地
     */
    private String origin;
    /**
     * 讫点地
     */
    private String destination;
    /**
     * 始发站
     */
    private String departureSt;
    /**
     * 讫点站
     */
    private String desSt;

    public String toJt2019String() {
        return "TRANS_TYPE:=" + valueToString(transType, "080")
            + ";VIN:=" + valueToString(vin)
            + ";VEHICLE_NATIONALITY:=" + valueToString(vehicleNationality)
            + ";VEHICLE_TYPE:=" + valueToString(vehicleType, "40")
            + ";RTPN:=" + valueToString(rtpn)
            + ";OPERATOR_NAME:=" + valueToString(operatorName)
            + ";OPERATOR_TEL:=" + valueToString(operatorTel)
            + ";OWERS_NAME:=" + valueToString(owersName)
            + ";OWERS_ORIG_ID:=" + valueToString(owersOrigId)
            + ";OWERS_TEL:=" + valueToString(owersTel)
            + ";RTOLN:=" + valueToString(rtoln)
            + ";VEHICLE_MODE:=" + valueToString(vehicleMode)
            + ";VEHICLE_COLOR:=" + valueToString(vehicleColor, "2")
            + ";VEHICLE_ORIG_ID:=" + valueToString(vehicleOrigId)
            + ";DRIVER_INFO:=" + valueToString(driverInfo)
            + ";BUSSINESS_AREA:=" + valueToString(businessArea)
            + ";BUSSINESSSCOPRECODE:=" + valueToString(businessScopeCode, "08100")
            + ";APPROVED_SEATS:=" + valueToString(approvedSeats)
            + ";ORIGIN:=" + valueToString(origin)
            + ";DESTINATION:=" + valueToString(destination)
            + ";DEPARTURE_ST:=" + valueToString(departureSt)
            + ";DES_ST:=" + valueToString(desSt);
    }

    public String toJt2013String() {
        return "TRANS_TYPE:=" + valueToString(transType, "080")
            + ";VIN:=" + valueToString(vin)
            + ";VEHICLE_NATIONALITY:=" + valueToString(vehicleNationality)
            + ";VEHICLE_TYPE:=" + valueToString(vehicleType, "40")
            + ";RTPN:=" + valueToString(rtpn)
            + ";OWERS_NAME:=" + valueToString(owersName)
            + ";OWERS_ORIG_ID:=" + valueToString(owersOrigId)
            + ";OWERS_TEL:=" + valueToString(owersTel)
            + ";RTOLN:=" + valueToString(rtoln)
            + ";VEHICLE_MODE:=" + valueToString(vehicleMode)
            + ";VEHICLE_COLOR:=" + valueToString(vehicleColor, "2")
            + ";VEHICLE_ORIG_ID:=" + valueToString(vehicleOrigId)
            + ";DRIVER_INFO:=" + valueToString(driverInfo)
            + ";BUSSINESS_AREA:=" + valueToString(businessArea)
            + ";BANLINE_TYPE:=" + valueToString(banLineType)
            + ";APPROVED_SEATS:=" + valueToString(approvedSeats)
            + ";ORIGIN:=" + valueToString(origin)
            + ";DESTINATION:=" + valueToString(destination)
            + ";DEPARTURE_ST:=" + valueToString(departureSt)
            + ";DES_ST:=" + valueToString(desSt);
    }

    private String valueToString(String value) {
        return StringUtils.isBlank(value) ? "" : value;
    }

    private String valueToString(String value, String defaultStr) {
        return StringUtils.isBlank(value) ? defaultStr : value;
    }
}
