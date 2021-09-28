package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;


/**
 * @Description:主动安全参数(吉标) (盲区参数设置)
 */
@Data
public class JiLinBlindArea implements T808MsgBody {

    /**
     * 后侧距离阈值
     */
    private Integer rear;

    /**
     * 侧后方距离阈值
     */
    private Integer sideRear;

    public JiLinBlindArea(AdasParamSettingForm paramSettingForm) {
        for (AdasAlarmParamSetting paramSetting : paramSettingForm.getAdasAlarmParamSettings()) {
            String key = paramSetting.getRiskFunctionId().toString();
            if ("176706".equals(key)) {
                this.rear =
                    (paramSetting.getRear() != null && paramSetting.getRear() != -1) ? paramSetting.getRear() : 0xff;
                this.sideRear = (paramSetting.getSideRear() != null && paramSetting.getSideRear() != -1)
                    ? paramSetting.getSideRear() : 0xff;
            }

        }
    }

}
