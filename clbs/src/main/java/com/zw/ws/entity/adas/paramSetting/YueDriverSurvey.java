package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:主动安全参数(粤标) (驾驶员状态监测参数设置)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class YueDriverSurvey extends PublicParameters implements T808MsgBody {

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
     * 摄像头遮挡报警分级速度阈值
     */
    private Integer occlusionSpeed = 0xff;
    /**
     * 玩手机报警触发车速阈值
     */
    private Integer playPhoneSpeed = 0xff;
    /**
     * 玩手机报警前后视频录制时间
     */
    private Integer playPhoneVideoTime = 0xff;
    /**
     * 玩手机报警拍照张数
     */
    private Integer playPhoneCameraNum = 0xff;
    /**
     * 玩手机报警拍照间隔时间
     */
    private Integer playPhoneCameraTime = 0xff;
    /**
     * 红外墨镜阻断失效报警触发车速阈值
     */
    private Integer blockingSpeed = 0xff;
    /**
     * 红外墨镜阻断失效报警前后视频录制时间
     */
    private Integer blockingVideoTime = 0xff;
    /**
     * 红外墨镜阻断失效报警拍照张数
     */
    private Integer blockingCameraNum = 0xff;
    /**
     * 红外墨镜阻断失效拍照间隔
     */
    private Integer blockingCameraTime = 0xff;
    /**
     * 未系安全带驾驶报警分级车速阈值
     */
    private Integer safetyBeltSpeed = 0xff;
    /**
     * 未系安全带驾驶报警前后视频录制时间
     */
    private Integer safetyBeltVideoTime = 0xff;
    /**
     * 未系安全带驾驶报警拍照张数
     */
    private Integer safetyBeltCameraNum = 0xff;
    /**
     * 未系安全带驾驶报警拍照时间间隔
     */
    private Integer safetyBeltCameraTime = 0xff;
    /**
     * 双手离把驾驶报警分级车速阈值
     */
    private Integer steeringSpeed = 0xff;
    /**
     * 双手离把驾驶报警前后视频录制时间
     */
    private Integer steeringtVideoTime = 0xff;
    /**
     * 双手离把驾驶报警拍照张数
     */
    private Integer steeringCameraNum = 0xff;
    /**
     * 双手离把驾驶报警拍照张数
     */
    private Integer steeringCameraTime = 0xff;

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
        alarmEnableMap.put("286501", Arrays.asList(0, 1));
        alarmEnableMap.put("286502", Arrays.asList(2, 3));
        alarmEnableMap.put("286503", Arrays.asList(4, 5));
        alarmEnableMap.put("286504", Arrays.asList(6, 7));
        alarmEnableMap.put("286505", Arrays.asList(8, 9));
        //最新变动
        alarmEnableMap.put("286508", Arrays.asList(10, 11));
        alarmEnableMap.put("286510", Arrays.asList(12, 13));
        alarmEnableMap.put("286511", Arrays.asList(14, 15));
        alarmEnableMap.put("286512", Arrays.asList(16, 17));
        alarmEnableMap.put("286513", Arrays.asList(18, 19));

        alarmParamMap
            .put("286501", Arrays.asList("fatigueSpeed", "fatigueCameraTime", "fatigueCameraNum", "fatigueVideoTime"));
        alarmParamMap.put("286502",
            Arrays.asList("pickUpSpeed", "pickUpCameraTime", "pickUpCameraNum", "pickUpVideoTime", "pickUpDecideTime"));
        alarmParamMap.put("286503", Arrays
            .asList("smokingSpeed", "smokingCameraTime", "smokingCameraNum", "smokingVideoTime", "smokingDecideTime"));

        alarmParamMap.put("286504",
            Arrays.asList("attentionSpeed", "attentionCameraTime", "attentionCameraNum", "attentionVideoTime"));

        alarmParamMap.put("286505",
            Arrays.asList("driveDeedSpeed", "driveDeedCameraTime", "driveDeedCameraNum", "driveDeedVideoTime"));
        //驾驶员更换事件（附带驾驶员身份识别触发）
        alarmParamMap.put("286507", Arrays.asList("driveDeedType"));
        //粤标调整新增
        alarmParamMap.put("286508", Arrays.asList("occlusionSpeed"));
        alarmParamMap.put("286510",
            Arrays.asList("safetyBeltSpeed", "safetyBeltCameraTime", "safetyBeltCameraNum", "safetyBeltVideoTime"));
        alarmParamMap.put("286511",
            Arrays.asList("blockingSpeed", "blockingCameraTime", "blockingCameraNum", "blockingVideoTime"));
        alarmParamMap.put("286512",
            Arrays.asList("steeringSpeed", "steeringCameraTime", "steeringCameraNum", "steeringtVideoTime"));
        alarmParamMap.put("286513",
            Arrays.asList("playPhoneSpeed", "playPhoneCameraTime", "playPhoneCameraNum", "playPhoneVideoTime"));

        //事件使能顺序维护
        eventEnableMap.put("286507", 0);
        eventEnableMap.put("286506", 1);
    }

    public YueDriverSurvey(AdasParamSettingForm paramSettingForm) {
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
        return key.equals("286506");
    }

    /**
     * 是否摄像头遮挡
     * @param key
     * @return
     */
    private boolean isOcclusion(String key) {
        return key.equals("286508");
    }

    private boolean isDriverChange(String key) {
        return key.equals("286507");
    }

    private void handelAlarmParam(AdasAlarmParamSetting paramSetting, String key) {

        List<String> fields = alarmParamMap.get(key);
        if (CollectionUtils.isEmpty(fields)) {
            return;
        }
        //如果是驾驶员身份识别，直接返回
        if (isDriverChange(key)) {
            setValIfPresent(fields.get(0), paramSetting.getTouchStatus());
            return;
        }
        setValIfPresent(fields.get(0), paramSetting.getAlarmLevelSpeedThreshold());
        //摄像头遮挡，直接返回
        if (isOcclusion(key)) {
            return;
        }
        //必须严格按照顺序来，不能错乱

        setValIfPresent(fields.get(1), paramSetting.getPhotographTime());
        setValIfPresent(fields.get(2), paramSetting.getPhotographNumber());
        setValIfPresent(fields.get(3), paramSetting.getVideoRecordingTime());

        //抽烟和接打电话多一个判断时间间隔
        if (isPhoneAndSmoke(fields)) {
            setValIfPresent(fields.get(4), paramSetting.getTimeSlotThreshold());
        }
    }

    private boolean isPhoneAndSmoke(List<String> fields) {
        return fields.size() == 5;
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
