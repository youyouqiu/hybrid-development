package com.zw.ws.impl;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.vas.alram.AlarmSetting;
import com.zw.platform.domain.vas.alram.query.ElectricitySet;
import com.zw.platform.domain.vas.alram.query.TerminalSet;
import com.zw.platform.push.cache.ParamSendingCache;
import com.zw.platform.push.cache.SendModule;
import com.zw.platform.push.cache.SendTarget;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.protocol.util.ProtocolTypeUtil;
import com.zw.ws.entity.t808.parameter.ParamItem;
import com.zw.ws.entity.t808.parameter.T808_0x8103;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiangxiaoqiang on 2016/12/6. 报警处理
 */
@Component
public class WsAlarmService {
    private static final Logger logger = LogManager.getLogger(WsAlarmService.class);

    @Autowired
    private ParamSendingCache paramSendingCache;

    /**
     * 报警类型
     */
    private static final String SPEEDING_WARNING = "超速预警";

    private static final String FATIGUE_DRIVING_WARNING = "疲劳驾驶预警";

    private static final String CRASH_WARNING = "碰撞预警";

    private static final String CRASH_ROLLOVER_WARNING = "碰撞侧翻报警";

    private static final String ROLLOVER_WARNING = "侧翻预警";

    private static final String SPEEDING_ALARM = "超速报警";

    private static final String FATIGUE_DRIVING_ALARM = "疲劳驾驶";

    private static final String TOTAL_DAY_DRIVING_TIMEOUT_ALARM = "当天累积驾驶超时";

    private static final String PARK_TIMEOUT_ALARM = "超时停车";

    private static final String ILLEGAL_AHIFT_ALARM = "车辆非法位移";

    private static final String ACC_ABNORMAL_NAME = "ACC信号异常报警";
    private static final String POSITIONAL_ABNORAML_NAME = "位置信息异常报警";
    private static final String ILLEGAL_DRIVING_ALARM = "违规行驶报警";

    private static final String POWER_ALARM = "设备电量报警";
    private static final String QUICK_ACCELERATION_ALARM = "急加速报警";
    private static final String SHARP_DECELERATION_ALARM = "急减速报警";
    private static final String SHARP_TURN_ALARM = "急转弯报警";
    private static final String CRASH_ALARM = "碰撞报警";

    private static final int ILLEGAL_DRIVING_ALARM_TIME_FRAME = 0X0032;

    //电量检测设置下发
    private static final int ELECTRIC_QUANTITY_DETECTION = 0XF34F;
    //终端检测设置下发
    private static final int TERMINAL_DTECTION_SETTING = 0XF350;
    /**
     * 报警类型id
     */
    private static final int SPEEDING_WARNING_ID = 0x005B; // 超速预警

    private static final int FATIGUE_DRIVING_WARNING_ID = 0x005C; // 疲劳驾驶预警

    private static final int CRASH_WARNING_ID = 0x005D; // 碰撞预警

    private static final int ROLLOVER_WARNING_ID = 0x005E; // 侧翻预警

    private static final int SPEEDING_ALARM_TOP_ID = 0x0055; // 最高速度

    private static final int SPEEDING_ALARM_TIME_ID = 0x0056; // 超速持续时间

    private static final int FATIGUE_DRIVING_ALARM_TIME_ID = 0x0057; // 连续驾驶时间门限

    private static final int FATIGUE_DRIVING_ALARM_BREAK_TIME_ID = 0x0059; // 最小休息时间

    private static final int TOTAL_DAY_DRIVING_TIMEOUT_ALARM_ID = 0x0058; // 当天累积驾驶超时

    private static final int PARK_TIMEOUT_ALARM_ID = 0x005A; // 超时停车

    private static final int ILLEGAL_AHIFT_ALARM_ID = 0x0031;// 车辆非法位移

    private static final int IGNORE_WARNING_ID = 0x0050; // 报警屏蔽设置

    private static final int ACC_ABNORMAL_CONTINUR_TIME = 0X0104;//acc信号异常: 异常持续时间
    private static final int ACC_ABNORMAL_MAX_VALUE = 0X0105;//acc信号异常: 最高速度，单位：km/h
    private static final int POSITIONAL_ABNORAML_CONTINUR_TIME = 0X0106;//位置信息异常报警: 异常持续时间，单位：分钟

    /**
     * 报警参数设置下发
     */
    public void alarmSettingCompose(List<AlarmSetting> alarmSettingList, Integer transNo, BindDTO bindDTO) {
        T808_0x8103 parameter = new T808_0x8103();
        if (alarmSettingList != null && alarmSettingList.size() > 0) {
            List<ParamItem> paramItemList = new ArrayList<>();
            int count = 0;
            // 碰撞预警 碰撞时间 默认值为200
            int crashValue1 = 200;
            // 碰撞预警 碰撞加速度 默认值为10
            int crashValue2 = 10;
            // 是否有碰撞预警参数1
            boolean crashFlag1 = false;
            // 是否有碰撞预警参数2
            boolean crashFlag2 = false;
            long ignore = 0L;
            boolean deviceTypeIs2019Flag = ProtocolTypeUtil.getAll2019Protocol().contains(bindDTO.getDeviceType());
            //是否下发高精度
            boolean isSendHighPrecision = false;
            TerminalSet terminalSet = new TerminalSet();
            for (AlarmSetting alarmSetting : alarmSettingList) {
                // 报警类型
                String alarmTypeName = alarmSetting.getName();
                // 是否下发
                String sendFlag = alarmSetting.getSendFlag();
                // 下发参数值
                String paramValue = alarmSetting.getParameterValue();
                boolean paramValueIsNotBlank = StringUtils.isNotBlank(paramValue);
                // 报警参数code
                String paramCode = alarmSetting.getParamCode();
                //屏蔽设置
                ignore = setIgnore(alarmSetting, ignore);
                if (!"1".equals(sendFlag)) {
                    continue;
                }
                // 碰撞预警
                if (CRASH_WARNING.contentEquals(alarmTypeName) || CRASH_ROLLOVER_WARNING.contentEquals(alarmTypeName)) {
                    if ("param1".equals(paramCode)) {
                        if (!paramValueIsNotBlank) {
                            continue;
                        }
                        if (deviceTypeIs2019Flag) {
                            // 2019版本的碰撞侧翻报警 -> 碰撞时间,单位：毫秒
                            crashValue1 = Integer.parseInt(paramValue);
                        } else {
                            // 单位 ms——>4ms
                            crashValue1 = Integer.parseInt(paramValue) / 4;
                        }
                        crashFlag1 = true;
                    } else if ("param2".equals(paramCode)) {
                        if (!paramValueIsNotBlank) {
                            continue;
                        }
                        crashValue2 = Integer.parseInt(paramValue);
                        crashFlag2 = true;
                    }
                    // 不是碰撞预警
                } else {
                    ParamItem paramItem = new ParamItem();
                    // 超速预警
                    if (SPEEDING_WARNING.contentEquals(alarmTypeName)) {
                        paramItem.setParamId(SPEEDING_WARNING_ID);
                        paramItem.setParamLength(2);
                        if (!paramValueIsNotBlank) {
                            continue;
                        }
                        int value = Integer.parseInt(paramValue);
                        // 单位 1/10 km/h
                        paramItem.setParamValue(value * 10);
                        // 疲劳预警
                    } else if (FATIGUE_DRIVING_WARNING.contentEquals(alarmTypeName) && paramValueIsNotBlank) {
                        getOtherParamItem(paramValue, paramItem, FATIGUE_DRIVING_WARNING_ID, 2);
                        // 侧翻预警
                    } else if (ROLLOVER_WARNING.contentEquals(alarmTypeName) && paramValueIsNotBlank) {
                        getOtherParamItem(paramValue, paramItem, ROLLOVER_WARNING_ID, 2);
                        // 超速报警
                    } else if (SPEEDING_ALARM.contentEquals(alarmTypeName)) {
                        // 最高速度
                        if ("param1".equals(paramCode)) {
                            getOtherParamItem(paramValue, paramItem, SPEEDING_ALARM_TOP_ID, 4);
                            // 超速持续时间
                        } else if ("param2".equals(paramCode)) {
                            getOtherParamItem(paramValue, paramItem, SPEEDING_ALARM_TIME_ID, 4);
                        }
                        // 疲劳驾驶
                    } else if (FATIGUE_DRIVING_ALARM.contentEquals(alarmTypeName)) {
                        // 连续驾驶门限
                        if ("param1".equals(paramCode)) {
                            getOtherParamItem(paramValue, paramItem, FATIGUE_DRIVING_ALARM_TIME_ID, 4);
                            // 最小休息时间
                        } else if ("param2".equals(paramCode)) {
                            getOtherParamItem(paramValue, paramItem, FATIGUE_DRIVING_ALARM_BREAK_TIME_ID, 4);
                        }
                        // 当天累计驾驶
                    } else if (TOTAL_DAY_DRIVING_TIMEOUT_ALARM.contentEquals(alarmTypeName)) {
                        getOtherParamItem(paramValue, paramItem, TOTAL_DAY_DRIVING_TIMEOUT_ALARM_ID, 4);
                        // 超时停车
                    } else if (PARK_TIMEOUT_ALARM.contentEquals(alarmTypeName)) {
                        getOtherParamItem(paramValue, paramItem, PARK_TIMEOUT_ALARM_ID, 4);
                        // 非法位移
                    } else if (ILLEGAL_AHIFT_ALARM.contentEquals(alarmTypeName)) {
                        getOtherParamItem(paramValue, paramItem, ILLEGAL_AHIFT_ALARM_ID, 2);
                    } else if (ACC_ABNORMAL_NAME.contentEquals(alarmTypeName)) {
                        // 异常持续时间
                        if ("param1".equals(paramCode)) {
                            if (paramValueIsNotBlank) {
                                getOtherParamItem(paramValue, paramItem, ACC_ABNORMAL_CONTINUR_TIME, 4);
                            }
                        } else if ("param2".equals(paramCode)) {
                            // 最高速度
                            if (paramValueIsNotBlank) {
                                getOtherParamItem(paramValue, paramItem, ACC_ABNORMAL_MAX_VALUE, 4);
                            }
                        }
                    } else if (POSITIONAL_ABNORAML_NAME.contentEquals(alarmTypeName)) {
                        getOtherParamItem(paramValue, paramItem, POSITIONAL_ABNORAML_CONTINUR_TIME, 2);
                    } else if (ILLEGAL_DRIVING_ALARM.contentEquals(alarmTypeName) && deviceTypeIs2019Flag) {
                        getIllegalDrivingParam(paramValue, paramItem);
                    } else if (POWER_ALARM.contains(alarmTypeName)) {
                        getPowerAlarmParam(paramValue, paramItem);
                    } else if (QUICK_ACCELERATION_ALARM.contains(alarmTypeName) && paramValueIsNotBlank) {
                        isSendHighPrecision = true;
                        terminalSet.setSpeedUpAlarm(Integer.parseInt(paramValue));
                    } else if (SHARP_DECELERATION_ALARM.contains(alarmTypeName) && paramValueIsNotBlank) {
                        isSendHighPrecision = true;
                        terminalSet.setSpeedCutAlarm(Integer.parseInt(paramValue));
                    } else if (SHARP_TURN_ALARM.contains(alarmTypeName) && paramValueIsNotBlank) {
                        isSendHighPrecision = true;
                        terminalSet.setSwerveAlarm(Integer.parseInt(paramValue));
                    } else if (CRASH_ALARM.contains(alarmTypeName) && paramValueIsNotBlank) {
                        isSendHighPrecision = true;
                        terminalSet.setCollisionAlarm(Integer.parseInt(paramValue));
                    }
                    if (paramValueIsNotBlank && !filterHighPrecision(alarmTypeName) && paramItem.getParamId() != null) {
                        paramItemList.add(paramItem);
                        count++;
                    }
                }
            }
            if (isSendHighPrecision) {
                ParamItem highPrecisionParamItem = new ParamItem();
                highPrecisionParamItem.setParamId(TERMINAL_DTECTION_SETTING);
                highPrecisionParamItem.setParamLength(20);
                highPrecisionParamItem.setParamValue(terminalSet);
                paramItemList.add(highPrecisionParamItem);
                count++;
            }
            //屏蔽设置
            ParamItem ignoreParamItem = new ParamItem();
            ignoreParamItem.setParamId(IGNORE_WARNING_ID);
            ignoreParamItem.setParamLength(4);
            ignoreParamItem.setParamValue(ignore);
            paramItemList.add(ignoreParamItem);
            count++;
            if (deviceTypeIs2019Flag && crashFlag1 && crashFlag2) {
                // 2019版碰撞时间、碰撞加速度都不为空时,才下发
                getCrashParam(paramItemList, crashValue1, crashValue2);
                count++;
            } else {
                if (crashFlag1 || crashFlag2) {
                    // 2013版本 是否有碰撞预警,如果参数1和参数2都没有设置则不下发,如果只设置其中一个另一个设为默认值
                    getCrashParam(paramItemList, crashValue1, crashValue2);
                    count++;
                }
            }
            try {
                parameter.setParametersCount(count);
                parameter.setPackageSum(count);
                parameter.setParamItems(paramItemList);
                String deviceId = bindDTO.getDeviceId();
                String userName = SystemHelper.getCurrentUsername();
                String simCard = bindDTO.getSimCardNumber();
                // 订阅推送消息
                SubscibeInfo info = new SubscibeInfo(userName, deviceId, transNo, ConstantUtil.T808_DEVICE_GE_ACK);
                SubscibeInfoCache.getInstance().putTable(info);
                paramSendingCache.put(SystemHelper.getCurrentUsername(), transNo, simCard,
                    SendTarget.getInstance(SendModule.ALARM_PARAMETER_SETTING));
                //参数设置设置缓存,暂时先注释掉，等待后续打开
                // paramSendingCache.put(userName, transNo, simCard, SendModule.ALARM_PARAMETER_SETTING);
                T808Message message = MsgUtil
                    .get808Message(simCard, ConstantUtil.T808_SET_PARAM, transNo, parameter, bindDTO.getDeviceType());
                WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_PARAM, deviceId);
            } catch (Exception e) {
                logger.error("WsAlarmService类异常" + e);
            }
        }
    }

    private void getOtherParamItem(String paramValue, ParamItem paramItem, int paramId, int paramLength) {
        paramItem.setParamId(paramId);
        paramItem.setParamLength(paramLength);
        if (StringUtils.isNotBlank(paramValue)) {
            int value = Integer.parseInt(paramValue);
            paramItem.setParamValue(value);
        }
    }

    private void getIllegalDrivingParam(String paramValue, ParamItem paramItem) {
        paramItem.setParamId(ILLEGAL_DRIVING_ALARM_TIME_FRAME);
        paramItem.setParamLength(4);
        if (StringUtils.isNotBlank(paramValue)) {
            String[] alarmSplit = paramValue.replace(" ", "").split("--");
            JSONObject paramValueObj = new JSONObject();
            String[] startArr = alarmSplit[0].split(":");
            String[] endArr = alarmSplit[1].split(":");
            paramValueObj.put("startHour", startArr[0]);
            paramValueObj.put("startMinute", startArr[1]);
            paramValueObj.put("endHour", endArr[0]);
            paramValueObj.put("endMinute", endArr[1]);
            paramItem.setParamValue(paramValueObj);
        }
    }

    private void getPowerAlarmParam(String paramValue, ParamItem paramItem) {
        paramItem.setParamId(ELECTRIC_QUANTITY_DETECTION);
        paramItem.setParamLength(57);
        if (StringUtils.isNotBlank(paramValue)) {
            ElectricitySet electricitySet = new ElectricitySet();
            electricitySet.setDeviceElectricity(Integer.parseInt(paramValue));
            paramItem.setParamValue(electricitySet);
        }
    }

    private boolean filterHighPrecision(String alarmTypeName) {
        return QUICK_ACCELERATION_ALARM.contains(alarmTypeName) || SHARP_DECELERATION_ALARM.contains(alarmTypeName)
            || SHARP_TURN_ALARM.contains(alarmTypeName) || CRASH_ALARM.contains(alarmTypeName);
    }

    /**
     * 碰撞报警
     * @param paramItemList paramItemList
     * @param crashValue1   crashValue1
     * @param crashValue2   crashValue2
     */
    private void getCrashParam(List<ParamItem> paramItemList, int crashValue1, int crashValue2) {
        ParamItem paramItem = new ParamItem();
        paramItem.setParamId(CRASH_WARNING_ID);
        paramItem.setParamLength(2);
        paramItem.setParamValue(crashValue1 + (crashValue2 << 8));
        paramItemList.add(paramItem);
    }

    public long setIgnore(AlarmSetting alarmSetting, long ignore) {
        int pos = Integer.parseInt(alarmSetting.getPos());
        Integer alarmPush = alarmSetting.getAlarmPush();
        // 1 屏蔽  0 不屏蔽
        int flag = (alarmPush == -1 || alarmPush == -2) ? 1 : 0;
        if (flag == 0) {
            return ignore;
        }
        if (pos == 158) {
            //碰撞预警/碰撞侧翻报警
            pos = 29;
        }
        if (pos == 157) {
            //危险预警/
            pos = 3;
        }
        if (pos > 31) {
            // 31以下是终端报警
            return ignore;
        }
        return ignore | 1L << pos;
    }

}
