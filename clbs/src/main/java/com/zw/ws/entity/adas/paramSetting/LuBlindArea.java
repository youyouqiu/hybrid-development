package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.platform.util.IntegerUtil;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

/**
 * @Description:主动安全参数(鲁标) (盲点检测参数设置)
 */
@Data
public class LuBlindArea implements T808MsgBody {

    /**
     * 后侧距离阈值DriverComparison
     */
    private Integer rear;

    /**
     * 侧后方距离阈值
     */
    private Integer sideRear;

    public LuBlindArea(AdasParamSettingForm paramSettingForm) {
        for (AdasAlarmParamSetting paramSetting : paramSettingForm.getAdasAlarmParamSettings()) {
            String key = paramSetting.getRiskFunctionId().toString();
            if (!"266701".equals(key)) {
                continue;
            }
            rear = IntegerUtil.getBlindVal(paramSetting.getRear());
            sideRear = IntegerUtil.getBlindVal(paramSetting.getSideRear());
        }
    }

}
