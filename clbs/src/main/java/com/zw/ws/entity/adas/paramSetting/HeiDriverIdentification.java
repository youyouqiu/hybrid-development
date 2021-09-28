package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import lombok.Data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: zjc
 * @Description:驾驶员身份识别参数下发实体（黑标）
 * @Date: create in 2020/11/24 10:13
 */
@Data
public class HeiDriverIdentification extends PublicParameters {

    /**
     * IC 卡从业资格证读卡失败报警前后视频录制时间
     */
    private Integer icCardVideoTime = 0xFF;
    /**
     * IC 卡从业资格证读卡失败报警拍照张数
     */
    private Integer icCardCameraNum = 0xFF;

    /**
     * IC 卡从业资格证读卡失败报警拍照间隔
     */
    private Integer icCardCameraTime = 0xFF;
    /**
     * 人证不符事件-前后视频录制时间
     */

    private Integer identVideoTime = 0xFF;
    /**
     * 人证不符事件-抓拍照片张数
     */
    private Integer identCameraNum = 0xFF;
    /**
     * 人证不符事件-拍照间隔
     */
    private Integer identCameraTime = 0xFF;
    /**
     * 保留字段
     */
    private byte[] keep1 = new byte[10];

    private static Map<String, List<String>> alarmParamMap = new HashMap<>();

    static {
        //报警类型设置那几个字段
        alarmParamMap.put("253801", Arrays.asList("identVideoTime", "identCameraTime", "identCameraNum"));
        alarmParamMap.put("253802", Arrays.asList("icCardVideoTime", "icCardCameraTime", "icCardCameraNum"));

    }

    public HeiDriverIdentification(AdasParamSettingForm paramSettingForm) {
        super(paramSettingForm.getCommonParamSetting());
        for (AdasAlarmParamSetting paramSetting : paramSettingForm.getAdasAlarmParamSettings()) {
            String functionId = paramSetting.getRiskFunctionId().toString();
            //组装报警事件参数设置
            handelAlarmParam(paramSetting, functionId);
        }
    }

    private void handelAlarmParam(AdasAlarmParamSetting paramSetting, String functionId) {
        List<String> fields = alarmParamMap.get(functionId);
        //这里需要严格控制顺序，不能错乱
        setValIfPresent(fields.get(0), paramSetting.getVideoRecordingTime());
        setValIfPresent(fields.get(1), paramSetting.getPhotographTime());
        setValIfPresent(fields.get(2), paramSetting.getPhotographNumber());
    }

}
