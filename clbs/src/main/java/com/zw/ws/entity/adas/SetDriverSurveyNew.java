package com.zw.ws.entity.adas;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;


/**
 * @Description:主动安全参数(川冀标) (驾驶员行为参数设置)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SetDriverSurveyNew extends SetDriveNew implements T808MsgBody {

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
     * （川） 长时间不目视前方
     * （冀）分神驾驶
     * 报警分 级车速阈值
     */
    private Integer attentionSpeed = 0xff;

    /**
     * （川） 长时间不目视前方
     * <p>
     * （冀）分神驾驶
     * <p>
     * 前后视频录制时间
     */
    private Integer attentionVideoTime = 0xff;

    /**
     * （川） 长时间不目视前方
     * <p>
     * （冀）分神驾驶
     * <p>
     * 报警拍照张数
     */
    private Integer attentionCameraNum = 0xff;

    /**
     * （川） 长时间不目视前方
     * <p>
     * （冀）分神驾驶
     * <p>
     * 报警拍照间隔时间
     */
    private Integer attentionCameraTime = 0xff;

    /**
     * （川）未检出到驾驶员
     * <p>
     * （冀）驾驶行为异常
     * <p>
     * 分级速度阈值
     */
    private Integer driveDeedSpeed = 0xff;

    /**
     * （川）未检出到驾驶员
     * <p>
     * （冀）驾驶行为异常
     * <p>
     * 视频录制时间
     */
    private Integer driveDeedVideoTime = 0xff;

    /**
     * （川）未检出到驾驶员
     * <p>
     * （冀）驾驶行为异常
     * <p>
     * 抓拍照片张数
     */
    private Integer driveDeedCameraNum = 0xff;

    /**
     * （川）未检出到驾驶员
     * <p>
     * （冀）驾驶行为异常
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
     * 川冀标驾驶员行为报警使能顺序
     */
    private static Map<String, Object> surveyAlarmEnableMap = new HashMap();

    /**
     * 川冀标驾驶员行为报警事件参数设置参数交互字段
     */
    private static Map<String, Object> surveyAlarmParamMap = new HashMap();

    /**
     * 6513 疲劳
     * 6502 接打手持电话
     * 6503 抽烟
     * 6508 长时间不目视前方（川），分神驾驶（冀）
     * 6510 未检测到驾驶员（川），驾驶员行为异常（冀）
     */
    static {
        //报警使能顺序维护
        String[][] alarmEnableOrder =
            {{"6513", "0,1"}, {"6502", "2,3"}, {"6503", "4,5"}, {"6508", "6,7"}, {"6505", "6,7"}, {"6510", "8,9"},
                {"6504", "8,9"}};
        for (String[] ints : alarmEnableOrder) {
            surveyAlarmEnableMap.put(ints[0], ints[1]);
        }
        //报警事件参数设置参数交互字段维护
        String[][] assistAlarmParamOrder =
            {{"6513", "fatigueSpeed,fatigueVideoTime,fatigueCameraNum,fatigueCameraTime"},
                {"6502", "pickUpSpeed,pickUpVideoTime,pickUpCameraNum,pickUpCameraTime"},
                {"6503", "smokingSpeed,smokingVideoTime,smokingCameraNum,smokingCameraTime"},
                {"6508", "attentionSpeed,attentionVideoTime,attentionCameraNum,attentionCameraTime"},
                {"6510", "driveDeedSpeed,driveDeedVideoTime,driveDeedCameraNum,driveDeedCameraTime"},
                {"6505", "attentionSpeed,attentionVideoTime,attentionCameraNum,attentionCameraTime"},
                {"6504", "driveDeedSpeed,driveDeedVideoTime,driveDeedCameraNum,driveDeedCameraTime"}
            };
        for (String[] ints : assistAlarmParamOrder) {
            surveyAlarmParamMap.put(ints[0], ints[1]);
        }
    }

    public SetDriverSurveyNew(AdasParamSettingForm paramSettingForm) {
        super(paramSettingForm.getCommonParamSetting());
        for (AdasAlarmParamSetting paramSetting : paramSettingForm.getAdasAlarmParamSettings()) {
            //截取川冀标的事件id，12,13标示
            String functionId = paramSetting.getRiskFunctionId().toString();
            String key = functionId.substring(2, functionId.length());
            //组装报警使能
            handelAlarmEnable(paramSetting, key);
            //组装事件使能
            //handleEventEnable(paramSetting, key);
            //组装报警事件参数设置
            handelAlarmParam(paramSetting, key);
        }
    }

    private void handelAlarmParam(AdasAlarmParamSetting paramSetting, String key) {
        //驾驶员身份识别
        if ("6515".equals(key)) {
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
        if ("6503".equals(key)) {
            setValIfPresent("smokingDecideTime", paramSetting.getTimeSlotThreshold());
        }
        if ("6502".equals(key)) {
            setValIfPresent("pickUpDecideTime", paramSetting.getTimeSlotThreshold());
        }
    }

    private void handelAlarmEnable(AdasAlarmParamSetting paramSetting, String key) {
        //驾驶员身份识别不计算报警使能
        if ("6515".equals(key)) {
            return;
        }
        String[] order = surveyAlarmEnableMap.get(key).toString().split(",");
        alarmEnable = calBinaryData(alarmEnable, paramSetting.getOneLevelAlarmEnable(), Integer.parseInt(order[0]));
        alarmEnable = calBinaryData(alarmEnable, paramSetting.getTwoLevelAlarmEnable(), Integer.parseInt(order[1]));
    }

    private void handleEventEnable(AdasAlarmParamSetting paramSetting, String key) {
        //驾驶员变更
        if ("6516".equals(key)) {
            eventEnable = calBinaryData(eventEnable, paramSetting.getRoadSignRecognition(), 0);
        }

    }

}
