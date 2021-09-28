package com.zw.adas.domain.define.setting;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @Author zhangqiang
 * @Date 2020/6/10 10:32
 */
@Data
public class AdasJingParamSetting {
    private String id;
    /**
     * 风险事件 （function_id）
     */
    private Integer riskFunctionId;
    /**
     * 参数下发id
     */
    private String parameterId;

    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 报警级别
     */
    private String alarmLevel;
    /**
     * 报警提示音量
     */
    private String alarmVolume;
    /**
     * 语音播报：0不播报，1播报，默认1
     */
    private Integer speech;
    /**
     * 报警视频时长 单位秒，取值范围0~300：0不采集视频，默认值6
     */
    private Integer alarmVideoDuration;
    /**
     * 报警视频分辨率
     */
    private String videoResolution;
    /**
     * 报警照片张数
     */
    private Integer photographNumber;
    /**
     * 照片分辨率
     */
    private String cameraResolution;
    /**
     * 拍照间隔
     */
    private Integer photographTime;
    /**
     * 速度阈值
     */
    private Integer speedLimit;
    /**
     * 判断持续时长阀值
     */
    private Integer durationThreshold;
    /**
     * 协议类型
     */
    private Integer protocolType;
    /**
     * 报警指令类型
     */
    private Integer paramType;

    private int flag = 1;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdasJingParamSetting that = (AdasJingParamSetting) o;
        return Objects.equals(riskFunctionId, that.riskFunctionId) && Objects.equals(vehicleId, that.vehicleId)
            && Objects.equals(protocolType, that.protocolType) && Objects.equals(paramType, that.paramType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(riskFunctionId, vehicleId, protocolType, paramType);
    }

    public static List<AdasJingParamSetting> convertList(String alarmParam) {
        JSONArray array = JSON.parseArray(alarmParam);
        List<AdasJingParamSetting> adasParamSettingForms = new ArrayList<>();
        if (array == null) {
            return adasParamSettingForms;
        }
        for (Object o : array) {
            JSONArray paramSettingArray = JSON.parseArray(JSON.toJSONString(o));
            for (Object setting : paramSettingArray) {
                AdasJingParamSetting adasParamSettingForm =
                    JSON.parseObject(JSON.toJSONString(setting), AdasJingParamSetting.class);
                adasParamSettingForms.add(adasParamSettingForm);
            }
        }
        return adasParamSettingForms;
    }
}
