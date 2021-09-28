package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

/**
 * @Description:主动安全参数(川标) (盲区参数设置)
 */
@Data
public class ZhongBlindArea implements T808MsgBody {

    /**
     * 盲区监测
     */
    private static String Blind_spot_monitoring = "216704";

    /**
     * 左侧距离阈值
     */
    private Integer left;

    /**
     * 右侧距离阈值
     */
    private Integer right;

    /**
     * 后侧距离阈值
     */
    private Integer rear;

    public ZhongBlindArea(AdasParamSettingForm paramSettingForm) {
        for (AdasAlarmParamSetting paramSetting : paramSettingForm.getAdasAlarmParamSettings()) {
            String key = paramSetting.getRiskFunctionId().toString();
            if (Blind_spot_monitoring.equals(key)) {
                this.rear =
                    (paramSetting.getRear() != null && paramSetting.getRear() != -1) ? paramSetting.getRear() : 0xff;
                this.left = (paramSetting.getLeftDistance() != null && paramSetting.getLeftDistance() != -1)
                    ? paramSetting.getLeftDistance() : 0xff;
                this.right = (paramSetting.getRightDistance() != null && paramSetting.getRightDistance() != -1)
                    ? paramSetting.getRightDistance() : 0xff;
            }

        }
    }

}
