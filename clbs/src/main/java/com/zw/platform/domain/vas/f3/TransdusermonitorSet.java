package com.zw.platform.domain.vas.f3;

import javax.validation.constraints.Pattern;

import org.apache.bval.constraints.NotEmpty;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 温度传感器/湿度传感器/正反转传感器
 * @author chengjingyu
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TransdusermonitorSet extends BaseFormBean {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "【车辆id】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String vehicleId;//车辆Id

    @NotEmpty(message = "【传感器id】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String sensorId;//传感器Id

    private String sensorOutId;//传感器外设Id

    private String sensorOutName;//传感器外设名称

    private String sensorNumber;//传感器型号

    private String baudrateName;//波特率名称

    private Integer baudrate;//波特率

    private String oddEvenCheckName;//奇偶校验名称

    private Integer oddEvenCheck;//奇偶校验

    private String compensateName;//补偿使能名称

    private Integer compensate;//补偿使能

    private Integer sensorType;//传感器类别

    private String autotimeName;//自动上传时间名字

    private String monitorType;//对象类型 0：车 1：人 2：物

    @Pattern(message = "【自动上传时间】输入错误，只能输入01,02,03,04;其中01:被动,02:10s,03:20s,04:30s！",
        regexp = "^[0][1-4]{1}$", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer autoTime;//自动上传时间

    private Integer filterFactor;//滤波系数

    private String filterFactorName;//滤波系数名称

    private String remark;//备注

    private Double alarmUp;//报警上阈值

    private Double alarmDown;//报警下阈值

    private Integer overValve;//超出阈值时间阈值

    private Integer correctionFactorK;//输出修正系数K

    private Integer correctionFactorB;//输出修正系数B

    private String brand;//车牌号

    private String groupName;//车辆所属组织名称

    private String groupId;//车辆所属组织id

    private String vehicleType;//车辆类型

    private Integer sendStatus;//下发状态

    private String detectionMode;

    public TransdusermonitorSet() {
    }

    public TransdusermonitorSet(String[] parameterList) {
        this.setSensorOutId(parameterList[0]);
        this.setSensorId(parameterList[1]);
        this.setVehicleId(parameterList[2]);
        this.setAutoTime(Integer.parseInt(parameterList[3]));
        this.setRemark(parameterList[4]);
        if (parameterList.length > 5 && !parameterList[5].isEmpty()) {
            this.setOverValve(Integer.parseInt(parameterList[5]));
        }
        if (parameterList.length > 6 && !parameterList[6].isEmpty()) {
            this.setCorrectionFactorK(Integer.parseInt(parameterList[6]));
        }
        if (parameterList.length > 7 && !parameterList[7].isEmpty()) {
            this.setCorrectionFactorB(Integer.parseInt(parameterList[7]));
        }
        if (parameterList.length > 8) {
            this.setAlarmUp(Double.parseDouble(parameterList[8]));
            this.setAlarmDown(Double.parseDouble(parameterList[9]));
        }
    }

}
