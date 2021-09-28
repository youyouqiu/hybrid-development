package com.zw.platform.domain.vas.alram.form;

import com.zw.platform.domain.vas.alram.AlarmSetting;
import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Description:报警参数设置Form
 * @author:wangying
 * @time:2016年12月8日 下午5:22:38
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class AlarmParameterSettingForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String vehicleId; // 车辆id
    private String brand; // 车牌号
    private String alarmParameterId; // 报警参数id
    private Integer alarmPush;//报警推送设置（0、无 1、局部 2、全局 -1、屏蔽）
    private String parameterValue; // 报警参数设置值
    private String pos; //字典标识
    @Deprecated
    private Integer ignore = 0; //屏蔽字段： 如果不等于,表示忽略对应的字段 （字段取消）
    private String name; //报警类型名
    private String paramCode;//参数编号
    /**
     * 报警类型id
     */
    private String alarmTypeId;
    /**
     * 所属模块
     */
    private String type;

    public AlarmParameterSettingForm(AlarmSetting alarmSetting) {
        this.alarmPush = alarmSetting.getAlarmPush();
        this.parameterValue = alarmSetting.getParameterValue();
        this.pos = alarmSetting.getPos();
        this.name = alarmSetting.getName();
        this.paramCode = alarmSetting.getParamCode();
        this.type = alarmSetting.getType();
    }
}
