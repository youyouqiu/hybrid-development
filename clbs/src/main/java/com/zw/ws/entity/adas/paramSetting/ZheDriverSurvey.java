package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;


/**
 * @Description:主动安全参数(浙标) (驾驶员行为参数设置)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ZheDriverSurvey extends PublicParameters implements T808MsgBody {

    /**
     * 报警使能
     */
    private Long alarmEnable = 0L;

    /**
     * 事件使能
     */
    private Long eventEnable = 3L;

    /**
     * 吸烟报警判断时间间隔
     */
    private Integer smokingDecideTime = 0xFFFF;

    /**
     * 接打手持电话报警判断时间间隔
     */
    private Integer pickUpDecideTime = 0xFFFF;

    /**
     * 预留
     */
    private byte[] keep1 = new byte[3];

    /**
     * 疲劳驾驶报警分级速度阈值
     */
    private Integer fatigueSpeed = 0xff;

    /**
     * 疲劳驾驶报警前后视频录制时间
     */
    private Integer fatigueVideoTime = 0xff;

    /**
     * 疲劳驾驶报警拍照张数
     */
    private Integer fatigueCameraNum = 0xff;

    /**
     * 疲劳驾驶报警拍照间隔时间
     */
    private Integer fatigueCameraTime = 0xff;

    /**
     * 接打电话报警分级速度阈值
     */
    private Integer pickUpSpeed = 0xff;

    /**
     * 接打电话报警前后视频录制时间
     */
    private Integer pickUpVideoTime = 0xff;

    /**
     * 接打电话报警拍驾驶员完整面部特征照片张数
     */
    private Integer pickUpCameraNum = 0xff;

    /**
     * 接打电话报警拍驾驶员完整面部特征照片间隔时间
     */
    private Integer pickUpCameraTime = 0xff;

    /**
     * 抽烟报警分级车速阈值
     */
    private Integer smokingSpeed = 0xff;

    /**
     * 抽烟报警前后视频录制时间
     */
    private Integer smokingVideoTime = 0xff;

    /**
     * 抽烟报警拍驾驶员完整面部特征照片张数
     */
    private Integer smokingCameraNum = 0xff;

    /**
     * 抽烟报警拍驾驶员完整面部特征照片间隔时间
     */
    private Integer smokingCameraTime = 0xff;

    /**
     * 分神驾驶
     * 报警分 级车速阈值
     */
    private Integer attentionSpeed = 0xff;

    /**
     * 分神驾驶
     * <p>
     * 前后视频录制时间
     */
    private Integer attentionVideoTime = 0xff;

    /**
     * 分神驾驶
     * 报警拍照张数
     */
    private Integer attentionCameraNum = 0xff;

    /**
     * 分神驾驶
     * <p>
     * 报警拍照间隔时间
     */
    private Integer attentionCameraTime = 0xff;

    /**
     * 驾驶行为异常
     * <p>
     * 分级速度阈值
     */
    private Integer driveDeedSpeed = 0xff;

    /**
     * 驾驶行为异常
     * <p>
     * 视频录制时间
     */
    private Integer driveDeedVideoTime = 0xff;

    /**
     * 驾驶行为异常
     * <p>
     * 抓拍照片张数
     */
    private Integer driveDeedCameraNum = 0xff;

    /**
     * 驾驶行为异常
     * <p>
     * 拍照间隔
     */
    private Integer driveDeedCameraTime = 0xff;

    /**
     * 驾驶员身份识别触发
     */
    private Integer driveDeedType = 0xff;

    /**
     * 换人驾驶报警触发车速阈值
     */
    private Integer substitutionSpeed = 0xff;

    /**
     * 换人驾驶报警前后视频录制时间
     */
    private Integer substitutionVideoTime = 0xff;

    /**
     * 换人驾驶报警拍照张数
     */
    private Integer substitutionCameraNum = 0xff;

    /**
     * 换人驾驶报警拍照间隔时间
     */
    private Integer substitutionCameraTime = 0xff;

    /**
     * 超时驾驶报警触发车速阈值
     */
    private Integer overTimeSpeed = 0xff;

    /**
     * 超时驾驶报警前后视频录制时间
     */
    private Integer overTimeVideoTime = 0xff;

    /**
     * 超时驾驶报警拍照张数
     */
    private Integer overTimeCameraNum = 0xff;

    /**
     * 超时驾驶报警拍照间隔时间
     */
    private Integer overTimenCameraTime = 0xff;

    /**
     * 日间连续驾驶时间阀值
     */
    private Integer dayTime = 0xff;

    /**
     * 夜间连续驾驶时间阀值
     */
    private Integer nightTime = 0xff;

    /**
     * 最小停车休息时间阀值
     */
    private Integer minStopTime = 0xff;

    /**
     * 夜间定义时段
     */
    private Integer nightTimeValue = 0xff;

    /**
     * 24 小时累计驾驶时间阀值
     */
    private Integer oneDayDriveTime = 0xff;

    /**
     * 预留
     */
    private byte[] keep2 = new byte[2];

    /**
     * 川冀标驾驶员行为报警使能顺序
     */
    private static Map<String, Object> surveyAlarmEnableMap = new HashMap();

    /**
     * 川冀标驾驶员行为报警事件参数设置参数交互字段
     */
    private static Map<String, Object> surveyAlarmParamMap = new HashMap();

    /**
     * 166513 疲劳
     * 166502 接打手持电话
     * 166503 抽烟
     * 166508 分神驾驶
     * 166504 驾驶行为异常(驾驶员异常报警)
     * 166516 探头遮挡
     * 166515 换人驾驶
     * 166514 超时驾驶
     *
     *
     */
    static {
        //报警使能顺序维护
        String[][] alarmEnableOrder =
            {{"166513", "0,1"}, {"166502", "2,3"}, {"166503", "4,5"}, {"166508", "6,7"}, {"166504", "8,9"},
                {"166516", "10"}, {"166515", "11"}, {"166514", "12"}};
        for (String[] ints : alarmEnableOrder) {
            surveyAlarmEnableMap.put(ints[0], ints[1]);
        }
        //报警事件参数设置参数交互字段维护
        String[][] assistAlarmParamOrder =
            {{"166513", "fatigueSpeed,fatigueVideoTime,fatigueCameraNum,fatigueCameraTime"},
                {"166502", "pickUpSpeed,pickUpVideoTime,pickUpCameraNum,pickUpCameraTime"},
                {"166503", "smokingSpeed,smokingVideoTime,smokingCameraNum,smokingCameraTime"},
                {"166508", "attentionSpeed,attentionVideoTime,attentionCameraNum,attentionCameraTime"},
                {"166504", "driveDeedSpeed,driveDeedVideoTime,driveDeedCameraNum,driveDeedCameraTime"},
                {"166515", "substitutionSpeed,substitutionVideoTime,substitutionCameraNum,substitutionCameraTime"},
                {"166514", "overTimeSpeed,overTimeVideoTime,overTimeCameraNum,overTimenCameraTime"}
            };
        for (String[] ints : assistAlarmParamOrder) {
            surveyAlarmParamMap.put(ints[0], ints[1]);
        }
    }

    public ZheDriverSurvey(AdasParamSettingForm paramSettingForm) {
        super(paramSettingForm.getCommonParamSetting());
        for (AdasAlarmParamSetting paramSetting : paramSettingForm.getAdasAlarmParamSettings()) {
            String functionId = paramSetting.getRiskFunctionId().toString();
            //组装报警使能
            handelAlarmEnable(paramSetting, functionId);
            //组装报警事件参数设置
            handelAlarmParam(paramSetting, functionId);
        }
    }

    private void handelAlarmParam(AdasAlarmParamSetting paramSetting, String key) {
        //探头遮挡报警
        if ("166516".equals(key)) {
            return;
        }
        //驾驶员身份识别
        if ("166517".equals(key)) {
            setValIfPresent("driveDeedType", paramSetting.getTouchStatus());
            return;
        }

        String[] params = surveyAlarmParamMap.get(key).toString().split(",");
        //分级速度阀值
        setValIfPresent(params[0], paramSetting.getAlarmLevelSpeedThreshold());
        //报警录制时间
        setValIfPresent(params[1], paramSetting.getVideoRecordingTime());
        //报警拍照张数
        setValIfPresent(params[2], paramSetting.getPhotographNumber());
        //报警拍照间隔
        setValIfPresent(params[3], paramSetting.getPhotographTime());

        //166514 超时驾驶
        if ("166514".equals(key)) {
            this.dayTime = paramSetting.getDayTime();
            this.nightTime = paramSetting.getNightTime();
            this.minStopTime = paramSetting.getMinStopTime();
            this.nightTimeValue = paramSetting.getNightTimeValue();
            this.oneDayDriveTime = paramSetting.getOneDayDriveTime();
        }
        if ("166503".equals(key)) {
            setValIfPresent("smokingDecideTime", paramSetting.getTimeSlotThreshold());
        }
        if ("166502".equals(key)) {
            setValIfPresent("pickUpDecideTime", paramSetting.getTimeSlotThreshold());
        }
    }

    private void handelAlarmEnable(AdasAlarmParamSetting paramSetting, String key) {
        if (surveyAlarmEnableMap.get(key) != null) {
            //166516 探头遮挡  166515 换人驾驶   166514超时驾驶
            if ("166516".equals(key) || "166515".equals(key) || "166514".equals(key)) {
                alarmEnable = calBinaryData(alarmEnable, paramSetting.getRoadSignEnable(),
                    Integer.parseInt(surveyAlarmEnableMap.get(key).toString()));
                return;
            }
            String[] order = surveyAlarmEnableMap.get(key).toString().split(",");
            alarmEnable = calBinaryData(alarmEnable, paramSetting.getOneLevelAlarmEnable(), Integer.parseInt(order[0]));
            alarmEnable = calBinaryData(alarmEnable, paramSetting.getTwoLevelAlarmEnable(), Integer.parseInt(order[1]));
        }
    }
}
