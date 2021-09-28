package com.zw.adas.domain.riskManagement.form;

import com.zw.adas.ws.entity.AdasSetDriveAssist;
import com.zw.adas.ws.entity.AdasSetDriverSurvey;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;


@Data
@EqualsAndHashCode(callSuper = false)
public class AdasRiskEventVehicleConfigForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /*
     * 车辆id
     */
    private String vehicleId;

    /**
     * 车牌号
     */
    private String brand;

    /*
     * 风险事件id
     */
    private String riskId;

    /*
     * 低速风险等级
     */
    private String lowSpeedLevel;

    /**
     * 低速
     */
    private Integer lowSpeed;

    /*
     * 高速风险等级
     */
    private String highSpeedLevel;

    /*
     * 高速
     */
    private Integer highSpeed;

    /*
     * 报警前后录制时间
     */
    private Integer videoRecordingTime;

    /**
     * 拍照张数
     */
    private Integer photographNumber;

    /*
     * 拍照间隔时间
     */
    private Float photographTime;

    private String riskVehicleId;

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
     * 低速录制 0否,1是
     */
    private Integer lowSpeedRecording;

    /**
     * 高速录制 0否,1是
     */
    private Integer highSpeedRecording;

    /**
     * 提示音量
     */
    private Integer alarmVolume;

    /**
     * 灵敏度
     */
    private Integer sensitivity;

    /**
     * 一级报警
     */
    private Integer oneLevelAlarmEnable;

    /**
     * 二级报警
     */
    private Integer twoLevelAlarmEnable;

    /**
     * 道路标识报警使能
     */
    private Integer roadMarkAlarmEnable;

    /**
     * dsm adas 主动抓拍报警使能
     */
    private Integer initiativeCaptureAlarmEnable;

    /**
     * 一级报警语音使能
     */
    private Integer twoLevelVoiceEnable;

    /**
     * 二级报警语音使能
     */
    private Integer voiceEnable;

    /**
     * 道路标识语音使能,人证不符语言使能，驾驶员不在驾驶位置
     */
    private Integer oneLevelVoiceEnable;

    /**
     * dsm adas 主动抓拍语言使能
     */
    private Integer initiativeCaptureVoiceEnable;

    /**
     * 间隔时间
     */
    private Double timeInterval;

    /**
     * 其事件速度阈值，驾驶员不在驾驶位置事件开关，定时检查驾驶员开关
     */
    private Integer thresholdValue;

    /**
     * 定时拍照时间间隔
     */
    private Integer timingPhotoInterval;

    /**
     * 定距拍照距离间隔
     */
    private Integer distancePhotoInterval;

    /**
     * ADAS DSM拍照张数
     */
    private Integer timingPhoto;

    /**
     * ADAS DSM定距抓拍
     */
    private Integer distanceCapture;

    /**
     * ADAS DSM定时抓拍
     */
    private Integer timingCapture;

    /**
     * 次数阈值
     */
    private Integer numberThreshold;

    /**
     * ADAS DSM 拍照间隔
     */
    private Float dsmAdasTimeInterval;

    /**
     * 驾驶员不在驾驶位置事件开关，定时检查驾驶员开关
     */
    private Integer checkSwitch;

    public void initSendParam(AdasSetDriverSurvey setDriverSurvey, AdasSetDriveAssist setDriveAssist) {
        setDriverSurvey.init(this);
        setDriveAssist.init(this);
    }

    public static AdasRiskEventVehicleConfigForm getInstance(AdasRiskEventVehicleConfigForm config, String vehicleId) {
        AdasRiskEventVehicleConfigForm riskEventForm = new AdasRiskEventVehicleConfigForm();
        BeanUtils.copyProperties(config, riskEventForm);
        riskEventForm.setId(UUID.randomUUID().toString());
        riskEventForm.setVehicleId(vehicleId);
        riskEventForm.setCreateDataTime(new Date());
        riskEventForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
        return riskEventForm;
    }

    public void getInstance() {
    }
}
