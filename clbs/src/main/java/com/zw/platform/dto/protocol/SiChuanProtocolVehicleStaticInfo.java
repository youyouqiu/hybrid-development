package com.zw.platform.dto.protocol;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/9/28 17:20
 */
@Data
public class SiChuanProtocolVehicleStaticInfo {

    /**
     * 车辆识别码
     */
    private String vin;

    /**
     * 车牌号
     */
    private String vehiclePlate;

    /**
     * 车牌颜色
     */
    private Integer plateColor;

    /**
     * 所属运输企业
     */
    private String orgCa;

    /**
     * 管理织机构代码
     */
    private String manageCa;
    /**
     * 籍贯地
     */
    private String zone;
    /**
     * 车辆类型
     */
    private String vehicleType;
    /**
     * 监控配置项
     */
    private Long optional;
    /**
     * 道路运输证号
     */
    private String transNo;
    /**
     * 经营范围
     */
    private String businessScope;
    /**
     * 运输证有效期
     */
    private String validityBegin;

    /**
     * 运 输证 有效 期 止
     */
    private String validityEnd;

    /**
     * 座位/吨位
     */
    private Integer seatTon;
    /**
     * 发动机号
     */
    private String motorNo;
    /**
     * 车主姓名
     */
    private String owner;
    /**
     * 车主电话
     */
    private String ownerTel;
    /**
     * 车台安装日期
     */
    private String installDate;
    /**
     * 拍照参数
     */
    private String photoParam;
    /**
     * 视频参数
     */
    private String vedioParam;

    @Override
    public String toString() {
        return "VIN:=" + (vin == null ? "" : vin)
            + ";VEHICLE_PLATE:=" + (vehiclePlate == null ? "" : vehiclePlate)
            + ";PLATE_COLOR:=" + (plateColor == null ? "" : plateColor)
            + ";ORG_CA:=" + (orgCa == null ? "" : orgCa)
            + ";MANAGE_CA:=" + (manageCa == null ? "" : manageCa)
            + ";ZONE:=" + (zone == null ? "" : zone)
            + ";VEHICLE_TYPE:=" + (vehicleType == null ? "" : vehicleType)
            + ";OPTIONAL:=" + (optional == null ? "" : optional)
            + ";TRANS_NO:=" + (transNo == null ? "" : transNo)
            + ";BUSINESS_SCOPE:=" + (businessScope == null ? "" : businessScope)
            + ";VALIDITY_BEGIN:=" + (validityBegin == null ? "" : validityBegin)
            + ";VALIDITY_END:=" + (validityEnd == null ? "" : validityEnd)
            + ";SEAT_TON:=" + (seatTon == null ? "" : seatTon)
            + ";MOTOR_NO:=" + (motorNo == null ? "" : motorNo)
            + ";OWNER:=" + (owner == null ? "" : owner)
            + ";OWNER_TEL:=" + (ownerTel == null ? "" : ownerTel)
            + ";INSTALL_DATE:=" + (installDate == null ? "" : installDate)
            + ";PHOTO_PARAM:=" + (photoParam == null ? "" : photoParam)
            + ";VEDIO_PARAM:=" + (vedioParam == null ? "" : vedioParam);
    }
}
