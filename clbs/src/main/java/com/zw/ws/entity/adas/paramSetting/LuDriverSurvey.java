package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:主动安全参数(鲁标) (驾驶员状态监测参数设置)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LuDriverSurvey extends PublicParameters implements T808MsgBody {

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
     * 报警使能顺序
     */

    private static Map<String, List<Integer>> alarmEnableMap = new HashMap<>();
    /**
     * 报警参数
     */
    private static Map<String, List<String>> alarmParamMap = new HashMap();
    /**
     * 事件使能
     */
    private static Map<String, Integer> eventEnableMap = new HashMap();

    static {

        //报警使能顺序维护
        alarmEnableMap.put("266501", Arrays.asList(0, 1));
        alarmEnableMap.put("266502", Arrays.asList(2, 3));
        alarmEnableMap.put("266503", Arrays.asList(4, 5));
        alarmEnableMap.put("266504", Arrays.asList(6, 7));
        alarmEnableMap.put("266505", Arrays.asList(8, 9));
        //报警参数字段顺序维护
        alarmParamMap
            .put("266501", Arrays.asList("fatigueCameraTime", "fatigueCameraNum", "fatigueVideoTime", "fatigueSpeed"));
        alarmParamMap.put("266502",
            Arrays.asList("pickUpCameraTime", "pickUpCameraNum", "pickUpVideoTime", "pickUpSpeed", "pickUpDecideTime"));
        alarmParamMap.put("266503", Arrays
            .asList("smokingCameraTime", "smokingCameraNum", "smokingVideoTime", "smokingSpeed", "smokingDecideTime"));

        alarmParamMap.put("266504",
            Arrays.asList("attentionCameraTime", "attentionCameraNum", "attentionVideoTime", "attentionSpeed"));

        alarmParamMap.put("266505",
            Arrays.asList("driveDeedCameraTime", "driveDeedCameraNum", "driveDeedVideoTime", "driveDeedSpeed"));
        //驾驶员更换事件（附带驾驶员身份识别触发）
        alarmParamMap.put("266511", Arrays.asList("driveDeedType"));
        //事件使能顺序维护
        eventEnableMap.put("266511", 0);
        eventEnableMap.put("266510", 1);
    }

    public LuDriverSurvey(AdasParamSettingForm paramSettingForm) {
        super(paramSettingForm.getCommonParamSetting());
        for (AdasAlarmParamSetting paramSetting : paramSettingForm.getAdasAlarmParamSettings()) {
            String functionId = paramSetting.getRiskFunctionId().toString();
            //组装报警使能
            handelAlarmEnable(paramSetting, functionId);
            //组装事件使能
            handleEventEnable(paramSetting, functionId);
            //组装报警事件参数设置
            handelAlarmParam(paramSetting, functionId);

        }
    }

    private void handleEventEnable(AdasAlarmParamSetting paramSetting, String key) {
        Integer position = eventEnableMap.get(key);
        if (position == null) {
            return;
        }

        if (isDriverChange(key)) {
            eventEnable = calBinaryData(eventEnable, paramSetting.getDriverChangeEnable(), position);
            return;
        }
        if (isInitiativePicture(key)) {
            eventEnable = calBinaryData(eventEnable, paramSetting.getInitiativePictureEnable(), position);
            return;
        }
    }

    private boolean isInitiativePicture(String key) {
        return key.equals("266510");
    }

    private boolean isDriverChange(String key) {
        return key.equals("266511");
    }

    private void handelAlarmParam(AdasAlarmParamSetting paramSetting, String key) {

        List<String> fields = alarmParamMap.get(key);
        if (fields == null) {
            return;
        }
        //如果是驾驶员身份识别，直接返回
        if (isDriverIdentify(fields)) {
            setValIfPresent(fields.get(0), paramSetting.getTouchStatus());
            return;
        }
        //必须严格按照顺序来，不能错乱
        setValIfPresent(fields.get(0), paramSetting.getPhotographTime());
        setValIfPresent(fields.get(1), paramSetting.getPhotographNumber());
        setValIfPresent(fields.get(2), paramSetting.getVideoRecordingTime());
        setValIfPresent(fields.get(3), paramSetting.getAlarmLevelSpeedThreshold());
        //抽烟和接打电话多一个判断时间间隔
        if (isPhoneAndSmoke(fields)) {
            setValIfPresent(fields.get(4), paramSetting.getTimeSlotThreshold());
        }
    }

    private boolean isPhoneAndSmoke(List<String> fields) {
        return fields.size() == 5;
    }

    private boolean isDriverIdentify(List<String> fields) {
        return fields.size() == 1;
    }

    private void handelAlarmEnable(AdasAlarmParamSetting paramSetting, String key) {
        List<Integer> position = alarmEnableMap.get(key);
        if (position == null) {
            return;
        }
        alarmEnable = calBinaryData(alarmEnable, paramSetting.getOneLevelAlarmEnable(), position.get(0));
        alarmEnable = calBinaryData(alarmEnable, paramSetting.getTwoLevelAlarmEnable(), position.get(1));

    }
}
