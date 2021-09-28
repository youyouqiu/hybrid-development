package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.platform.util.IntegerUtil;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

/**
 * @Description:主动安全参数(鲁标) (驾驶员对比参数设置)
 */
@Data
public class LuDriverComparison implements T808MsgBody {

    /**
     * 离线人脸比对开关
     * <p>
     * 0：关闭
     * <p>
     * 1：打开
     */
    private Integer onOff;

    /**
     * 人脸比对(DSM 人脸图片)成功阈值
     * <p>
     * 驾驶员比对成功相似度阈值
     * <p>
     * 百分比；范围 0%~100%  单位是 1%
     */
    private Integer dsmSimilarityThreshold;

    /**
     * 人脸比对(手机人脸图片)成功阈值
     * <p>
     * 驾驶员比对成功相似度阈值
     * <p>
     * 百分比；范围 0%~100%  单位是 1%
     */
    private Integer phoneSimilarityThreshold;

    public LuDriverComparison(AdasParamSettingForm paramSettingForm) {
        for (AdasAlarmParamSetting paramSetting : paramSettingForm.getAdasAlarmParamSettings()) {
            String key = paramSetting.getRiskFunctionId().toString();
            if (!"262331".equals(key)) {
                continue;
            }
            onOff = paramSetting.getOfflineFaceCompareEnable();
            dsmSimilarityThreshold = IntegerUtil.getOrDefault(paramSetting.getDsmCompareSuccessPercent(), -1, 0);
            phoneSimilarityThreshold = IntegerUtil.getOrDefault(paramSetting.getPhoneCompareSuccessPercent(), -1, 0);

        }
    }

}
