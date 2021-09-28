package com.zw.platform.dto.protocol;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/9/25 15:00
 */
@Data
public class FreightVehicleDto {
    /**
     * 运输行业编码
     */
    private String transType;
    /**
     * 车牌号
     */
    private String vin;
    /**
     * 准牵引总质量
     */
    private String traction;
    /**
     * 挂车车牌号
     */
    private String trailerVin;

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
    private String vehcileOrigId;
    /**
     * 驾驶员情况
     */
    private String driverInfo;
    /**
     * 押运员情况
     */
    private String guardsInfo;
    /**
     * 核定吨位
     */
    private String approvedTonnage;
    /**
     * 危险品货物分类
     */
    private String dgType;
    /**
     * 经营代码范围
     */
    private String businessScopeCode;
    /**
     * 货物品名
     */
    private String cargoName;
    /**
     * 货物吨位
     */
    private String cargoTonnage;
    /**
     * 运输出发地
     */
    private String transportOrigin;
    /**
     * 运输目的地
     */
    private String transportDes;
    /**
     * 运输起止时间
     */
    private String tssl;

    public String toJt2019String() {
        return "TRANS_TYPE:=" + valueToString(transType, "030")
            + ";VIN:=" + valueToString(vin)
            + ";TRACTION:=" + valueToString(traction)
            + ";TRAILER_VIN:=" + valueToString(trailerVin, vin)
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
            + ";VEHICLE_ORIG_ID:=" + valueToString(vehcileOrigId)
            + ";DRIVER_INFO:=" + valueToString(driverInfo)
            + ";GUARDS_INFO:=" + valueToString(guardsInfo)
            + ";APPROVED_TONNAGE:=" + valueToString(approvedTonnage)
            + ";BUSINESSSCOPECODE:=" + valueToString(businessScopeCode)
            + ";CARGO_NAME:=" + valueToString(cargoName)
            + ";CARGO_TONNAGE:=" + valueToString(cargoTonnage)
            + ";TRANSPORT_ORIGIN:=" + valueToString(transportOrigin)
            + ";TRANSPORT_DES:=" + valueToString(transportDes)
            + ";TSSL:=" + valueToString(tssl);
    }

    public String toJt2013String() {
        return "TRANS_TYPE:=" + valueToString(transType, "030")
            + ";VIN:=" + valueToString(vin)
            + ";TRACTION:=" + valueToString(traction)
            + ";TRAILER_VIN:=" + valueToString(trailerVin, vin)
            + ";VEHICLE_NATIONALITY:=" + valueToString(vehicleNationality)
            + ";VEHICLE_TYPE:=" + valueToString(vehicleType, "40")
            + ";RTPN:=" + valueToString(rtpn)
            + ";OWERS_NAME:=" + valueToString(owersName)
            + ";OWERS_ORIG_ID:=" + valueToString(owersOrigId)
            + ";OWERS_TEL:=" + valueToString(owersTel)
            + ";RTOLN:=" + valueToString(rtoln)
            + ";VEHICLE_MODE:=" + valueToString(vehicleMode)
            + ";VEHICLE_COLOR:=" + valueToString(vehicleColor, "2")
            + ";VEHICLE_ORIG_ID:=" + valueToString(vehcileOrigId)
            + ";DRIVER_INFO:=" + valueToString(driverInfo)
            + ";GUARDS_INFO:=" + valueToString(guardsInfo)
            + ";APPROVED_TONNAGE:=" + valueToString(approvedTonnage)
            + ";DG_TYPE :=" + valueToString(dgType)
            + ";CARGO_NAME:=" + valueToString(cargoName)
            + ";CARGO_TONNAGE:=" + valueToString(cargoTonnage)
            + ";TRANSPORT_ORIGIN:=" + valueToString(transportOrigin)
            + ";TRANSPORT_DES:=" + valueToString(transportDes)
            + ";TSSL:=" + valueToString(tssl);
    }

    private String valueToString(String value) {
        return StringUtils.isBlank(value) ? "" : value;
    }

    private String valueToString(String value, String defaultStr) {
        return StringUtils.isBlank(value) ? defaultStr : value;
    }
}
