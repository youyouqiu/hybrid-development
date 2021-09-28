package com.zw.platform.domain.basicinfo.driverDiscernManage;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 驾驶员识别管理详情列表实体
 * @Author Tianzhangxu
 * @Date 2020/9/28 10:48
 */
@Data
public class DeviceDriverInfo implements Serializable {
    private static final long serialVersionUID = 3169981093088683321L;

    /**
     * 人脸id
     */
    private String professionalsId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 企业Id
     */
    private String groupId;

    /**
     * 从业资格证号
     */
    private String cardNumber;

    /**
     * 照片地址
     */
    private String photograph;

    /**
     * 人脸id
     */
    private String faceId;
}
