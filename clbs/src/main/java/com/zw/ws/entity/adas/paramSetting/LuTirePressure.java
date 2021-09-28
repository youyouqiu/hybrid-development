package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.platform.util.IntegerUtil;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

/**
 * @Description:主动安全参数(鲁标) (胎压参数设置)
 */
@Data
public class LuTirePressure implements T808MsgBody {

    /**
     * 轮胎型号
     */
    private String tyreNumber;

    /**
     * 胎压单位
     */
    private Integer unit;

    /**
     * 正常胎压值
     */
    private Integer pressure;

    /**
     * 胎压不平衡门限
     */
    private Integer pressureThreshold;

    /**
     * 慢漏气门限
     */
    private Integer slowLeakThreshold;

    /**
     * 低压阈值
     */
    private Integer lowPressure;

    /**
     * 高压阈值
     */
    private Integer heighPressure;

    /**
     * 高温阈值
     */
    private Integer highTemperature;

    /**
     * 电压阈值
     */
    private Integer electricityThreshold;

    /**
     * 定时上报时间间隔
     */
    private Integer uploadTime;

    /**
     * 保留项
     */
    private byte[] keep2 = new byte[6];

    //266601
    public LuTirePressure(AdasParamSettingForm paramSettingForm, String tyreNumberName) {
        for (AdasAlarmParamSetting paramSetting : paramSettingForm.getAdasAlarmParamSettings()) {
            String key = paramSetting.getRiskFunctionId().toString();
            if ("266601".equals(key)) {
                //轮胎型号默认参数:900R20(根据协议)
                tyreNumber = tyreNumberName != null ? tyreNumberName : "900R20";
                unit = IntegerUtil.getTireVal(paramSetting.getUnit());
                pressure = IntegerUtil.getTireVal(paramSetting.getPressure());
                pressureThreshold = IntegerUtil.getTireVal(paramSetting.getPressureThreshold());
                slowLeakThreshold = IntegerUtil.getTireVal(paramSetting.getSlowLeakThreshold());
                lowPressure = IntegerUtil.getTireVal(paramSetting.getLowPressure());
                heighPressure = IntegerUtil.getTireVal(paramSetting.getHighPressure());
                highTemperature = IntegerUtil.getTireVal(paramSetting.getHighTemperature());
                electricityThreshold = IntegerUtil.getTireVal(paramSetting.getElectricityThreshold());
                uploadTime = IntegerUtil.getTireVal(paramSetting.getUploadTime());
            }
        }
    }

}
