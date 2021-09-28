package com.zw.platform.domain.vas.loadmgt.form;

import com.zw.platform.domain.vas.loadmgt.PersonLoadParam;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import java.io.Serializable;

/***
 @Author gfw
 @Date 2018/9/11 15:42
 @Description 绑定参数
 @version 1.0
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class LoadVehicleSettingSensorForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = -6160724159175678422L;
    /**
     * 载重传感器---------------开始-----------------
     */
    /**
     * 传感器id
     */
    @NotEmpty(message = "【传感器型号】不能为空", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String sensorId;
    /**
     * 传感器型号
     */
    private String sensorNumber;
    /**
     * 传感器序号: 0:发动机1; 1:发动机2
     */
    private Integer sensorSequence;
    /**
     * 个性化参数
     */
    private PersonLoadParam personLoadParam;

    /**
     * 前端使用v1
     */
    private String personLoadJson;
    /**
     * 个性化JSON AD标定
     */
    private String adParamJson;
    /**
     * 载重传感器----------------结束----------------------
     *
     */
    /**
     * 载重传感器2-----------------开始----------------------
     *
     */
    /**
     * 传感器车辆关系表id
     */
    private String twoId;
    /**
     * 传感器id
     */
    private String twoSensorId;
    /**
     * 传感器型号
     */
    private String twoSensorNumber;
    /**
     * 传感器序号: 0:发动机1; 1:发动机2
     */
    private Integer twoSensorSequence;
    /**
     * 个性化参数
     */
    private PersonLoadParam twoPersonLoadParam;
    /**
     * 前端使用v1
     */
    private String twoPersonLoadJson;
    /**
     * 个性化JSON AD标定
     */
    private String twoAdParamJson;
    /**
     * 载重传感器2----------------结束-------------------------
     *
     */
    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 车牌号
     */
    private String plateNumber;

    /**
     * 参数id
     */
    private String paramId;

    /**
     * 监控对象类型 {1:车,2:物,3:人}
     */
    private Integer monitorType;

    /**
     * 个性化JSON
     */
    private String personLoadParamJSON;

    private String sensorOutId;
    private String twoSensorOutId;

}
