package com.zw.adas.service.defineSetting.impl;

import com.zw.adas.domain.riskManagement.form.AdasRiskEventVehicleConfigForm;
import com.zw.adas.service.core.AdasUserService;
import com.zw.adas.service.defineSetting.AdasActiveSafetyService;
import com.zw.adas.service.defineSetting.AdasRiskEventConfigService;
import com.zw.adas.service.defineSetting.AdasSendTxtService;
import com.zw.adas.ws.entity.AdasSetDriveAssist;
import com.zw.adas.ws.entity.AdasSetDriverSurvey;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.StringUtil;
import com.zw.ws.entity.t808.parameter.ParamItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class AdasActiveSafetyServiceImpl implements AdasActiveSafetyService {

    @Autowired
    private AdasSendTxtService adasSendTxtService;

    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Autowired
    private AdasRiskEventConfigService adasRiskEventConfigService;

    @Autowired
    private AdasUserService adasUserService;

    private static Logger log = LogManager.getLogger(AdasActiveSafetyServiceImpl.class);

    @Override
    public void sendParamSet(String vehicleIds, String ipAddress)
        throws Exception {
        List<String> vehicleIdList = getVehicleIdList(vehicleIds);
        Optional.ofNullable(vehicleIdList).ifPresent(vids -> vids.stream().forEach(vid -> sendParamByVid(vid)));
        Optional.ofNullable(vehicleIdList).ifPresent(vids -> addLogs(vids, ipAddress));
    }

    @Override
    public Map<String, Object> sendAdasParamter(String vehicleId, String ipAddress)
        throws Exception {
        Map<String, Object> result = new HashMap<>();
        Optional.ofNullable(vehicleId).ifPresent(vid -> sendAdasParamByVid(vid, result));
        Optional.ofNullable(getVehicleIdList(vehicleId)).ifPresent(vids -> addLogs(vids, ipAddress));
        return result;
    }

    @Override
    public Map<String, Object> sendDsmParamter(String vehicleId, String ipAddress)
        throws Exception {
        Map<String, Object> result = new HashMap<>();
        Optional.ofNullable(vehicleId).ifPresent(vid -> sendDsmParamByVid(vid, result));
        Optional.ofNullable(getVehicleIdList(vehicleId)).ifPresent(vids -> addLogs(vids, ipAddress));
        return result;
    }

    private void sendAdasParamByVid(String vid, Map<String, Object> result) {
        AdasSetDriveAssist setDriveAssist = new AdasSetDriveAssist();
        initAdas(setDriveAssist, vid);
        //??????ADAS?????????msgSN
        String msgSN = send(0xF3E1, setDriveAssist, vid, "F3-8103-64");
        String userName = SystemHelper.getCurrentUsername();
        result.put("msgId", msgSN);
        result.put("userName", userName);
    }

    private void initAdas(AdasSetDriveAssist setDriveAssist, String vid) {
        List<AdasRiskEventVehicleConfigForm> settingList = adasRiskEventConfigService.findRiskSettingByVid(vid);
        Optional.ofNullable(settingList).ifPresent(
            settings -> settings.stream().forEach(setting -> setDriveAssist.init(setting)));
    }

    private Map<String, Object> sendDsmParamByVid(String vid, Map<String, Object> result) {
        AdasSetDriverSurvey setDriverSurvey = new AdasSetDriverSurvey();
        initDsm(setDriverSurvey, vid);
        //??????DSM?????????msgSN
        String msgSN = send(0xF3E2, setDriverSurvey, vid, "F3-8103-65");
        String userName = SystemHelper.getCurrentUsername();
        result.put("msgId", msgSN);
        result.put("userName", userName);
        return result;

    }

    private void initDsm(AdasSetDriverSurvey setDriverSurvey, String vid) {
        List<AdasRiskEventVehicleConfigForm> settingList = adasRiskEventConfigService.findRiskSettingByVid(vid);
        Optional.ofNullable(settingList).ifPresent(
            settings -> settings.forEach(setDriverSurvey::init));
    }

    private void addLogs(List<String> vids, String ipAddress) {
        StringBuffer message = new StringBuffer();
        vids.forEach(vid -> initMessage(vid, message));
        try {
            if (vids.size() == 1) {
                String[] vehicle = logSearchServiceImpl.findCarMsg(vids.get(0));
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "2", "",
                    vehicle[0], vehicle[1]);
            } else {
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "2", "batch",
                    "ADAS????????????????????????");
            }
        } catch (Exception e) {
            log.error("??????adas???????????????????????????" + e);
        }
    }

    private void initMessage(String vid, StringBuffer message) {
        final Map<String, String> infoMap =
                RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(vid), "name", "orgName");
        String brand = infoMap.get("name");
        String groupName = infoMap.get("orgName");
        if (!brand.isEmpty() && !groupName.isEmpty()) {
            message.append("???????????? : ").append(brand).append(" ( @").append(groupName).append(" ) ?????????????????? <br/>");
        }
    }

    private void sendParamByVid(String vid) {
        AdasSetDriverSurvey setDriverSurvey = new AdasSetDriverSurvey();
        AdasSetDriveAssist setDriveAssist = new AdasSetDriveAssist();
        init(setDriverSurvey, setDriveAssist, vid);
        log.info("?????????ADAS???????????????" + JsonUtil.object2Json(setDriveAssist));
        log.info("?????????DSM???????????????" + JsonUtil.object2Json(setDriverSurvey));
        //??????ADAS
        send(0xF3E1, setDriveAssist, vid, "F3-8103-64");
        //??????DSM
        send(0xF3E2, setDriverSurvey, vid, "F3-8103-65");

    }

    private List<String> getVehicleIdList(String vehicleIds) {
        if (StringUtil.isNullOrBlank(vehicleIds)) {
            return null;
        } else {
            return Arrays.asList(vehicleIds.split(","));
        }
    }

    private String send(Integer parameterId, Object paramVal, String vid, String paramType) {
        List<ParamItem> driveParams = getSendParams(paramVal, parameterId);
        try {
            return adasSendTxtService.sendF3SetParam(vid, "ADAS_" + vid, driveParams, paramType, true,
                SystemHelper.getCurrentUsername());
        } catch (Exception e) {
            log.error("?????????????????????" + e);
            return "";
        }
    }

    private List<ParamItem> getSendParams(Object paramVal, Integer parameterId) {
        List<ParamItem> params = new ArrayList<>();
        ParamItem paramItem = new ParamItem();
        paramItem.setParamValue(paramVal);
        paramItem.setParamId(parameterId);
        paramItem.setParamLength(56);
        params.add(paramItem);
        return params;
    }

    private void init(AdasSetDriverSurvey setDrive, AdasSetDriveAssist setDriveAssist, String vehicleId) {
        List<AdasRiskEventVehicleConfigForm> settingList = adasRiskEventConfigService.findRiskSettingByVid(vehicleId);
        Optional.ofNullable(settingList).ifPresent(
            settings -> settings.stream().forEach(setting -> setting.initSendParam(setDrive, setDriveAssist)));
    }

}
