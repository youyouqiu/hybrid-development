package com.zw.adas.domain.riskManagement.form;

import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.riskManagement.RiskType;
import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * 风险事件定义（时间间隔P,持续时间T）
 *
 * @author Administrator
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AdasRiskEventVehicleForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /*
     * 车辆id
     */
    private String vehicleId;

    /*
     * 疲劳报警-时间间隔
     */
    private Integer fatigueP;

    /**
     * 疲劳报警-持续时长
     */
    private Integer fatigueT;

    /*
     * 分心报警-时间间隔
     */
    private Integer distractP;

    /*
     * 分心报警-持续时长
     */
    private Integer distractT;

    /*
     * 碰撞报警-时间间隔
     */
    private Integer collisionP;

    /**
     * 碰撞报警-持续时长
     */
    private Integer collisionT;

    /*
     * 异常报警-时间间隔
     */
    private Integer abnormalP;

    /*
     * 异常报警-持续时长
     */
    private Integer abnormalT;

    /**
     * 拍照分辨率
     */
    private String videoResolution;

    /**
     * 视频录制分辨率
     */

    private String cameraResolution;

    /**
     * 提示音量
     */
    private Integer alarmVolume;

    /**
     * '灵敏度'
     */
    private Integer sensitivity;

    public Map<String, String> initAndGetAssembleData(AdasRiskEventVehicleConfigForm config) {
        int riskType = RiskType.getRiskType(config.getRiskId());

        Map<String, String> data = new HashMap<>();
        Integer intervalTime = null;
        Integer continueTime = null;
        switch (riskType) {
            case 1:
                intervalTime = config.getFatigueP();
                continueTime = config.getFatigueT();
                fatigueP = intervalTime;
                fatigueT = continueTime;
                break;
            case 2:
                intervalTime = config.getDistractP();
                continueTime = config.getDistractT();
                distractP = intervalTime;
                distractT = continueTime;
                break;
            case 3:
                intervalTime = config.getAbnormalP();
                continueTime = config.getAbnormalT();
                abnormalP = intervalTime;
                abnormalT = continueTime;
                break;
            case 4:
                intervalTime = config.getCollisionP();
                continueTime = config.getCollisionT();
                collisionP = intervalTime;
                collisionT = continueTime;
                break;
            default:
                break;
        }

        Integer videoRecordingTime = Optional.ofNullable(config.getVideoRecordingTime()).orElse(10);
        Integer lowSpeed = config.getLowSpeed();
        if (riskType == 4) {
            lowSpeed = Optional.ofNullable(lowSpeed).orElse(30);
        } else {
            lowSpeed = Optional.ofNullable(lowSpeed).orElse(10);
        }
        if (StringUtils.isNotBlank(config.getLowSpeedLevel())) {
            data.put("riskLevel_1", config.getLowSpeedLevel());
        }
        if (StringUtils.isNotBlank(config.getHighSpeedLevel())) {
            data.put("riskLevel_2", config.getHighSpeedLevel());
        }
        data.put("intervalTime", intervalTime == null ? "" : intervalTime + "");
        data.put("continueTime", continueTime == null ? "" : continueTime + "");
        data.put("videoRecordingTime", String.valueOf(videoRecordingTime)); // 录制时间存入redis
        data.put("highSpeed",
                config.getHighSpeed() != null ? String.valueOf(config.getHighSpeed()) : "50");
        data.put("lowSpeed", lowSpeed + "");
        return data;

    }

    public void initRiskSetting(AdasRiskEventVehicleConfigForm setForm, String vehicleId) {
        // 风险设置
        this.vehicleId = vehicleId;
        cameraResolution = setForm.getCameraResolution();
        videoResolution = setForm.getVideoResolution();
        sensitivity = setForm.getSensitivity();
        alarmVolume = setForm.getAlarmVolume();
        setCreateDataTime(new Date());
        setCreateDataUsername(SystemHelper.getCurrentUsername());

    }

    public void initUpdateIssueSetting(AdasRiskEventVehicleConfigForm setForm) {
        //修正下发需要
        fatigueP = setForm.getFatigueP();
        fatigueT = setForm.getFatigueT();
        distractP = setForm.getDistractP();
        distractT = setForm.getDistractT();
        abnormalP = setForm.getAbnormalP();
        abnormalT = setForm.getAbnormalT();
        collisionP = setForm.getCollisionP();
        collisionT = setForm.getCollisionT();
    }

}
