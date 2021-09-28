package com.zw.platform.dto.protocol;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/9/28 17:21
 */
@Data
public class SiChuanProtocolProfessionalStaticInfo {
    /**
     * 18 位身份证号码
     */
    private String idNumber;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 服务运输企业
     */
    private String orgCa;

    /**
     * 服务运输车辆
     */
    private String vehicleVin;
    /**
     * 从业资格证号
     */
    private String qualificationNumber;
    /**
     * 从业资格类别
     */
    private String qualificationType;
    /**
     * 资格证有效期起
     */
    private String validityBegin;
    /**
     * 资格证有效期止
     */
    private String validityEnd;

    /**
     * 联系电话
     */
    private String tel;

    /**
     * 联系地址
     */
    private String address;

    @Override
    public String toString() {
        return "ID_NUMBER:=" + (idNumber == null ? "" : idNumber)
            + ";NAME:=" + (name == null ? "" : name)
            + ";SEX:=" + (sex == null ? "" : sex)
            + ";ORG_CA:=" + (orgCa == null ? "" : orgCa)
            + ";VEHICLE_VIN:=" + (vehicleVin == null ? "" : vehicleVin)
            + ";QUALIFICATION_NUMBER:=" + (qualificationNumber == null ? "" : qualificationNumber)
            + ";QUALIFICATION_TYPE:=" + (qualificationType == null ? "" : qualificationType)
            + ";VALIDITY_BEGIN:=" + (validityBegin == null ? "" : validityBegin)
            + ";VALIDITY_END:=" + (validityEnd == null ? "" : validityEnd)
            + ";TEL:=" + (tel == null ? "" : tel)
            + ";ADDRESS:=" + (address == null ? "" : address);
    }
}
