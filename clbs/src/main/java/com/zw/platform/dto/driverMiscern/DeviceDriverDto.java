package com.zw.platform.dto.driverMiscern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/9/24 15:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceDriverDto implements Serializable {
    private static final long serialVersionUID = -2482569824967471833L;
    /**
     * 人脸id
     */
    private String faceId;
    /**
     * 从业人员id
     */
    private String professionalsId;
    /**
     * 姓名
     */
    private String name;
    /**
     * 企业名称
     */
    private String orgName;
    /**
     * 从业资格证号
     */
    private String cardNumber;
    /**
     * 照片地址
     */
    private String photograph;
}
