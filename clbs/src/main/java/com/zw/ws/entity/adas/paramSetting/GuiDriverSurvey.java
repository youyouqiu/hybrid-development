package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;


/**
 * @Description:主动安全参数(桂标) (驾驶员行为参数设置)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GuiDriverSurvey extends PublicParameters implements T808MsgBody {

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
     * 长时间不目视前方
     * 报警分 级车速阈值
     */
    private Integer attentionSpeed = 0xff;

    /**
     * <p>
     * 长时间不目视前方
     * <p>
     * 前后视频录制时间
     */
    private Integer attentionVideoTime = 0xff;

    /**
     * <p>
     * 长时间不目视前方
     * <p>
     * 报警拍照张数
     */
    private Integer attentionCameraNum = 0xff;

    /**
     * <p>
     * 长时间不目视前方
     * <p>
     * 报警拍照间隔时间
     */
    private Integer attentionCameraTime = 0xff;

    /**
     * <p>
     * 未监测到驾驶员
     * <p>
     * 分级速度阈值
     */
    private Integer driveDeedSpeed = 0xff;

    /**
     * <p>
     * 未监测到驾驶员
     * <p>
     * 视频录制时间
     */
    private Integer driveDeedVideoTime = 0xff;

    /**
     * <p>
     * 未监测到驾驶员
     * <p>
     * 抓拍照片张数
     */
    private Integer driveDeedCameraNum = 0xff;

    /**
     * <p>
     * 未监测到驾驶员
     * <p>
     * 拍照间隔
     */
    private Integer driveDeedCameraTime = 0xff;

    /**
     * 驾驶员身份识别触发
     */
    private Integer driveDeedType = 0xff;

    /**
     * 预留
     */
    private byte[] keep2 = new byte[2];

    /**
     * 桂标驾驶员行为报警使能顺序
     */
    private static Map<String, Object> surveyAlarmEnableMap = new HashMap();

    /**
     * 桂标驾驶员行为报警事件参数设置参数交互字段
     */
    private static Map<String, Object> surveyAlarmParamMap = new HashMap();

    /**
     * 146513 疲劳
     * 146502 接打手持电话
     * 146503 抽烟
     * 146508 长时间不目视前方
     * 146510 未监测到驾驶员
     */
    static {
        //报警使能顺序维护
        String[][] alarmEnableOrder =
            {{"146513", "0,1"}, {"146502", "2,3"}, {"146503", "4,5"}, {"146508", "6,7"}, {"146510", "8,9"}};
        for (String[] ints : alarmEnableOrder) {
            surveyAlarmEnableMap.put(ints[0], ints[1]);
        }
        //报警事件参数设置参数交互字段维护
        String[][] assistAlarmParamOrder =
            {{"146513", "fatigueSpeed,fatigueVideoTime,fatigueCameraNum,fatigueCameraTime"},
                {"146502", "pickUpSpeed,pickUpVideoTime,pickUpCameraNum,pickUpCameraTime"},
                {"146503", "smokingSpeed,smokingVideoTime,smokingCameraNum,smokingCameraTime"},
                {"146508", "attentionSpeed,attentionVideoTime,attentionCameraNum,attentionCameraTime"},
                {"146510", "driveDeedSpeed,driveDeedVideoTime,driveDeedCameraNum,driveDeedCameraTime"}
            };
        for (String[] ints : assistAlarmParamOrder) {
            surveyAlarmParamMap.put(ints[0], ints[1]);
        }
    }

    public GuiDriverSurvey(AdasParamSettingForm paramSettingForm) {
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
        //驾驶员身份识别
        if ("146515".equals(key)) {
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
        if ("146503".equals(key)) {
            setValIfPresent("smokingDecideTime", paramSetting.getTimeSlotThreshold());
        }
        if ("146502".equals(key)) {
            setValIfPresent("pickUpDecideTime", paramSetting.getTimeSlotThreshold());
        }
    }

    private void handelAlarmEnable(AdasAlarmParamSetting paramSetting, String key) {
        if (surveyAlarmEnableMap.get(key) != null) {
            String[] order = surveyAlarmEnableMap.get(key).toString().split(",");
            alarmEnable = calBinaryData(alarmEnable, paramSetting.getOneLevelAlarmEnable(), Integer.parseInt(order[0]));
            alarmEnable = calBinaryData(alarmEnable, paramSetting.getTwoLevelAlarmEnable(), Integer.parseInt(order[1]));
        }
    }
}
