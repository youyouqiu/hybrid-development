package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasAlarmParamSettingDao;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @Description:主动安全参数(桂标) (胎压参数设置)
 */
@Data
public class GuiTirePressure implements T808MsgBody {

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

    @Autowired
    AdasAlarmParamSettingDao alarmParamDao;

    public GuiTirePressure(AdasParamSettingForm paramSettingForm, String tyreNumberName) {
        for (AdasAlarmParamSetting paramSetting : paramSettingForm.getAdasAlarmParamSettings()) {
            String key = paramSetting.getRiskFunctionId().toString();
            if ("146608".equals(key)) {
                //轮胎型号默认参数:900R20(根据协议)
                this.tyreNumber = tyreNumberName != null ? tyreNumberName : "900R20";
                this.unit =
                    (paramSetting.getUnit() != null && paramSetting.getUnit() != -1) ? paramSetting.getUnit() : 0xffff;
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
                this.uploadTime = (paramSetting.getUploadTime() != null && paramSetting.getUploadTime() != -1)
                    ? paramSetting.getUploadTime() : 0xffff;
            }
        }
    }

}
