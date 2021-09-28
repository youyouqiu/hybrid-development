package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.domain.basicinfo.TyrePressureParameter;
import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TyrePressureSettingForm extends BaseFormBean {

    private String vehicleId;

    private String brand;

    private Integer numberOfTires; //轮胎数量

    private String sensorId; //传感器id

    private String sensorName; //传感器型号

    private String vehicleType; //对象类型

    private String paramId; //下发参数id

    private String groupName; //所属企业

    private Integer compensate;//补偿使能 1:使能,2:禁用

    private Integer filterFactor;//滤波系数 1:实时,2:平滑,3:平稳

    private String compensateStr;

    private String filterFactorStr;

    private String groupId;

    private Integer status;

    private String monitorType; //车型类别

    /**
     * 个性设置
     */
    private TyrePressureParameter tyrePressureParameter;
    private String tyrePressureParameterStr; //个性参数json串
}
