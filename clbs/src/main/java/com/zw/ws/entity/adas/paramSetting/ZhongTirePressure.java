package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

/**
 * @Description:主动安全参数(川标) (胎压参数设置)
 */
@Data
public class ZhongTirePressure implements T808MsgBody {

    /**
     * 胎压监测
     */
    private static transient String TIRE_PRESSURE_MONITORING = "216608";
    /**
     * 补传使能
     */
    private Integer compensatingEnable = 0x01;
    /**
     * 滤波系数
     */
    private Integer smoothing = 0x02;
    /**
     * 自动上传时间
     */
    private Integer automaticUploadTime = 0x01;
    /**
     * 修正系数K
     */
    private Integer compensationFactorK = 100;
    /**
     * 修正系数B
     */
    private Integer compensationFactorB = 100;
    /**
     * 保留项补零
     */
    private byte[] keep1 = new byte[11];
    /**
     * 轮胎编号 (页面无设置)
     */
    private Integer number = 0xff;
    /**
     * 正常胎压值 1
     */
    private Integer pressure = 0xffff;
    /**
     * 胎压不平衡门限 1
     */
    private Integer pressureThreshold = 0xffff;
    /**
     * 慢漏气门限 1
     */
    private Integer slowLeakThreshold = 0xffff;
    /**
     * 低压阈值 1
     */
    private Integer lowPressure = 0xfffff;
    /**
     * 高压阈值 1
     */
    private Integer heighPressure = 0xffff;
    /**
     * 高温阈值 1
     */
    private Integer highTemperature = 0xffff;
    /**
     * 传感器电量报警阈值 1
     */
    private Integer electricityThreshold = 0xffff;
    /**
     * 保留项补零
     */
    private byte[] keep2 = new byte[20];

    public ZhongTirePressure(AdasParamSettingForm paramSettingForm) {
        for (AdasAlarmParamSetting paramSetting : paramSettingForm.getAdasAlarmParamSettings()) {
            String key = paramSetting.getRiskFunctionId().toString();
            if (TIRE_PRESSURE_MONITORING.equals(key)) {
                this.compensatingEnable =
                    (paramSetting.getCompensatingEnable() != null && paramSetting.getCompensatingEnable() != -1)
                        ? paramSetting.getCompensatingEnable() : 0x01;
                this.smoothing = (paramSetting.getSmoothing() != null && paramSetting.getSmoothing() != -1)
                    ? paramSetting.getSmoothing() : 0x02;
                this.automaticUploadTime =
                    (paramSetting.getAutomaticUploadTime() != null && paramSetting.getAutomaticUploadTime() != -1)
                        ? paramSetting.getAutomaticUploadTime() : 0x01;
                this.compensationFactorK =
                    (paramSetting.getCompensationFactorK() != null && paramSetting.getCompensationFactorK() != -1)
                        ? paramSetting.getCompensationFactorK() : 100;
                this.compensationFactorB =
                    (paramSetting.getCompensationFactorB() != null && paramSetting.getCompensationFactorB() != -1)
                        ? paramSetting.getCompensationFactorB() : 100;
                this.pressure = (paramSetting.getPressure() != null && paramSetting.getPressure() != -1)
                    ? paramSetting.getPressure() : 0xffff;
                this.pressureThreshold =
                    (paramSetting.getPressureThreshold() != null && paramSetting.getPressureThreshold() != -1)
                        ? paramSetting.getPressureThreshold() : 0xffff;
                this.slowLeakThreshold =
                    (paramSetting.getSlowLeakThreshold() != null && paramSetting.getSlowLeakThreshold() != -1)
                        ? paramSetting.getSlowLeakThreshold() : 0xffff;
                this.lowPressure = (paramSetting.getLowPressure() != null && paramSetting.getLowPressure() != -1)
                    ? paramSetting.getLowPressure() : 0xffff;
                this.heighPressure = (paramSetting.getHighPressure() != null && paramSetting.getHighPressure() != -1)
                    ? paramSetting.getHighPressure() : 0xffff;
                this.highTemperature =
                    (paramSetting.getHighTemperature() != null && paramSetting.getHighTemperature() != -1)
                        ? paramSetting.getHighTemperature() : 0xffff;
                this.electricityThreshold =
                    (paramSetting.getElectricityThreshold() != null && paramSetting.getElectricityThreshold() != -1)
                        ? paramSetting.getElectricityThreshold() : 0xffff;
            }
        }
    }

}
