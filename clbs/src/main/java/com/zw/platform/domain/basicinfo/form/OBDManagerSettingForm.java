package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author Administrator
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OBDManagerSettingForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 4402013147904574461L;

    /**
     * 车id
     */
    private String vehicleId;
    /**
     * 车牌
     */
    private String brand;
    /**
     * 所属企业名称
     */
    private String groupName;
    private String groupId;
    /**
     * 监控对象类型
     */
    private Integer monitorType;
    /**
     * obd车型id
     */
    private String obdVehicleTypeId;
    /**
     * obd车型分类
     */
    private String vehicleType;
    /**
     * obd车型id
     */
    private String code;
    /**
     * obd车型名称/发动机类型
     */
    private String obdVehicleName;
    /**
     * 间隔时间
     */
    private Integer time;
    /**
     * 协议类型
     */
    private String protocol;
    /**
     * 下发状态
     */
    private Integer status;

    private String paramId;
}
