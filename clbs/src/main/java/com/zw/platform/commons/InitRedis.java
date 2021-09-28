package com.zw.platform.commons;

import com.alibaba.fastjson.JSON;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.domain.riskManagement.RiskType;
import com.zw.platform.domain.riskManagement.form.RiskEventVehicleConfigForm;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.repository.vas.AlarmSearchDao;
import com.zw.platform.repository.vas.SensorPollingDao;
import com.zw.platform.service.riskManagement.RiskEventConfigService;
import com.zw.platform.util.CommonUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@DependsOn("redisHelper")
public class InitRedis {
    private static final Logger log = LogManager.getLogger(InitRedis.class);

    @Autowired
    private RiskEventConfigService riskEventConfigService;

    @Autowired
    private AlarmSearchDao alarmSearchDao;

    @Autowired
    private SensorPollingDao sensorPollingDao;

    /**
     * 根据功能id，判断风险类型
     * @param functionId 功能id
     * @return 风险类型
     */
    private static int getRiskType(String functionId) {
        int riskType;
        switch (functionId) {
            case "6401":
                riskType = RiskType.RISK_CRASH;
                break;
            case "6402":
                riskType = RiskType.RISK_CRASH;
                break;
            case "6403":
                riskType = RiskType.RISK_CRASH;
                break;
            case "6404":
                riskType = RiskType.RISK_CRASH;
                break;
            case "6405":
                riskType = RiskType.RISK_CRASH;
                break;
            case "6502":
                riskType = RiskType.RISK_DISTRACTION;
                break;
            case "6503":
                riskType = RiskType.RISK_DISTRACTION;
                break;
            case "6504":
                riskType = RiskType.RISK_DISTRACTION;
                break;
            case "6505":
                riskType = RiskType.RISK_EXCEPTION;
                break;
            case "6506":
                riskType = RiskType.RISK_TIRED;
                break;
            case "6507":
                riskType = RiskType.RISK_TIRED;
                break;
            case "6508":
                riskType = RiskType.RISK_TIRED;
                break;
            default:
                riskType = 0;
                break;
        }
        return riskType;
    }

    @PostConstruct
    public void initRedisMethod() {
        try {
            cacheRiskSettings();
            // redis数据清空重启后,将传感器绑定情况重新存到redis中
            cacheSensorBindings();
            // 程序启动时,将报警数据字典放到缓存里面
            cacheAlarmType();
        } catch (Exception e) {
            log.error("风控数据存入缓存异常", e);
        }
    }

    private void cacheRiskSettings() {
        List<RiskEventVehicleConfigForm> riskSettingList = riskEventConfigService.findAllRiskSetting();
        if (CollectionUtils.isEmpty(riskSettingList)) {
            return;
        }
        // 管道存储redis
        Map<RedisKey, Map<String, String>> redisKeyMapMap = new HashMap<>(riskSettingList.size());
        for (RiskEventVehicleConfigForm config : riskSettingList) {
            int riskType = getRiskType(config.getRiskId());
            if (!StringUtils.isNotBlank(config.getVehicleId()) || !StringUtils.isNotBlank(config.getRiskId())
                || riskType == 0) {
                continue;
            }

            String intervalTime = "";
            String continueTime = "";
            switch (riskType) {
                case 1:
                    intervalTime = String.valueOf(config.getFatigueP());
                    continueTime = String.valueOf(config.getFatigueT());
                    break;
                case 2:
                    intervalTime = String.valueOf(config.getDistractP());
                    continueTime = String.valueOf(config.getDistractT());
                    break;
                case 3:
                    intervalTime = String.valueOf(config.getAbnormalP());
                    continueTime = String.valueOf(config.getAbnormalT());
                    break;
                case 4:
                    intervalTime = String.valueOf(config.getCollisionP());
                    continueTime = String.valueOf(config.getCollisionT());
                    break;

                default:
                    break;
            }
            Integer videoRecordingTime = config.getVideoRecordingTime();
            if (videoRecordingTime == null) {
                videoRecordingTime = 10;
            }
            Map<String, String> data = new HashMap<>();
            if (StringUtils.isNotBlank(config.getLowSpeedLevel())) {
                data.put("riskLevel_1", config.getLowSpeedLevel());
            }
            if (StringUtils.isNotBlank(config.getHighSpeedLevel())) {
                data.put("riskLevel_2", config.getHighSpeedLevel());
            }
            data.put("intervalTime", intervalTime);
            data.put("continueTime", continueTime);
            // 录制时间存入redis
            data.put("videoRecordingTime", String.valueOf(videoRecordingTime));
            data.put("highSpeed", config.getHighSpeed() != null ? String.valueOf(config.getHighSpeed()) : "50");
            data.put("lowSpeed", String.valueOf(config.getLowSpeed()));
            redisKeyMapMap.put(
                HistoryRedisKeyEnum.ADAS_PARAM_VEHICLE_RISK_ID.of(config.getVehicleId(), config.getRiskId()), data);
        }
        RedisHelper.batchAddToHash(redisKeyMapMap);
    }

    private void cacheSensorBindings() {
        List<String> listConfigId = sensorPollingDao.findConfigId();
        if (CollectionUtils.isEmpty(listConfigId)) {
            return;
        }
        List<RedisKey> redisKey =
            listConfigId.stream().map(HistoryRedisKeyEnum.SENSOR_MESSAGE::of).collect(Collectors.toList());
        RedisHelper.batchAddToString(redisKey, "true");
    }

    private void cacheAlarmType() {
        // 获取到全部的报警数据字典
        List<AlarmType> alarmTypes = alarmSearchDao.getAlarmType("");
        if (CollectionUtils.isEmpty(alarmTypes)) {
            return;
        }
        Map<RedisKey, String> map = new HashMap<>(CommonUtil.ofMapCapacity(alarmTypes.size()));
        String value;
        for (AlarmType alarmType : alarmTypes) {
            String name = alarmType.getName();
            if (alarmType.getType().contains("platAlarm") || alarmType.getType().contains("PlatAlarm")) {
                name = name + "(平台)";
                alarmType.setName(name);
            }
            value = JSON.toJSONString(alarmType);
            if ("7901".equals(alarmType.getPos())) {
                map.put(HistoryRedisKeyEnum.ALARM_TYPE_INFO.of("203"), value);
            } else {
                map.put(HistoryRedisKeyEnum.ALARM_TYPE_INFO.of(alarmType.getPos()), value);
            }
        }
        RedisHelper.batchAddToString(map);
    }

}
