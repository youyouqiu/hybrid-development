package com.zw.platform.service.riskManagement.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasRiskEventConfigDao;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.event.ConfigUnBindEvent;
import com.zw.platform.domain.riskManagement.form.RiskEventVehicleConfigForm;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.vas.RiskEventConfigDao;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.service.riskManagement.RiskEventConfigService;
import com.zw.platform.util.common.VehicleUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by fanlu on 2017/8/22.
 */
@Service
@Slf4j
public class RiskEventConfigServiceImpl implements RiskEventConfigService {

    @Autowired
    private RiskEventConfigDao riskEventConfigDao;

    @Autowired
    private AdasRiskEventConfigDao adasRiskEventConfigDao;

    @Autowired
    private UserService userService;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    private static String[] eventCodes =
        { "6401", "6402", "6403", "6404", "6405", "6407", "6408", "6409", "6410", "6502", "6503", "6505", "6506",
            "6507", "6508", "6509", "6510" };

    @Override
    public void deleteRiskSettingByVehicleIds(List<String> vehicleIds, ConfigUnBindEvent unBindEvent, String sign) {
        if (vehicleIds != null && !vehicleIds.isEmpty()) {
            //???????????????????????????
            riskEventConfigDao.deleteRiskVehicleByBatch(vehicleIds);
            riskEventConfigDao.deleteRiskVehicleConfigByBatch(vehicleIds);
            //????????????????????????
            adasRiskEventConfigDao.deleteAdasCommonParameterByBatch(vehicleIds);
            adasRiskEventConfigDao.deleteAdasAlarmParameterByBatch(vehicleIds);
            StringBuilder message = new StringBuilder();
            for (String vid : vehicleIds) {
                // ??????redis??????
                RedisHelper.delByPattern(HistoryRedisKeyEnum.ADAS_PARAM_VEHICLE_RISK_ID_FUZZY.of(vid + "*"));
                // ??????????????????????????????
                if (!"2".equals(sign)) {
                    // ??????????????????, ???????????????; ?????????????????????????????????????????????, ????????????
                    parameterDao.deleteByVechicleidParameterName(vid, "ADAS_" + vid, "F3-8103-64");
                    parameterDao.deleteByVechicleidParameterName(vid, "ADAS_" + vid, "F3-8103-65");
                    //????????????????????????
                    parameterDao.deleteProtocolParameterByVechicleId(vid);
                    addDeleteRiskSettingLog(vid, sign, message);
                }
            }
            sendBatchDeleteRiskMsgToStorm(vehicleIds);
            addRemoveRiskSettingLog(vehicleIds, unBindEvent, sign, message);
        }
    }

    @EventListener
    public void updateVehicleUnbound(ConfigUnBindEvent unBindEvent) {
        if (Objects.equals("update", unBindEvent.getOperation())) {
            return;
        }
        List<String> monitorIds = unBindEvent.getUnbindList().stream().map(BindDTO::getId).collect(Collectors.toList());
        taskExecutor.execute(() -> {
            try {
                deleteRiskSettingByVehicleIds(monitorIds, unBindEvent, "2");
            } catch (Exception e) {
                log.error("??????????????????: ????????????????????????????????????????????????", e);
            }
        });
    }

    private void addDeleteRiskSettingLog(String vehicleId, String s, StringBuilder message) {
        if ("1".equals(s)) { // 1????????????????????????
            // ?????????????????????????????????
            final BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(vehicleId);
            if (bindInfo != null) {
                String brand = bindInfo.getName();
                String orgName = userService.getOrgByUuid(bindInfo.getOrgId()).getName();
                if (!brand.isEmpty() && !orgName.isEmpty()) {
                    message.append("???????????? : ").append(brand).append(" ( @").append(orgName).append(" ) ?????????????????? <br/>");
                }
            }
        }
    }

    private void sendBatchDeleteRiskMsgToStorm(List<String> vehicleIds) {
        List<String> parameterList = Lists.newLinkedList();
        for (String eventCode : eventCodes) {
            for (String vehicleId : vehicleIds) {
                parameterList.add("#del#RISK_" + vehicleId + "_" + eventCode);
                if (!parameterList.contains("#del#RISK_" + vehicleId + "_AUTO")) {
                    //????????????????????????
                    parameterList.add("#del#RISK_" + vehicleId + "_AUTO");
                }
            }
        }
        sendMessage(parameterList);
    }

    private void addRemoveRiskSettingLog(List<String> vehicleIds, ConfigUnBindEvent unBindEvent, String sign,
        StringBuilder message) {
        if ("1".equals(sign)) { // 1????????????????????????
            if (vehicleIds.size() == 1) {
                String[] vehicle = logSearchServiceImpl.findCarMsg(vehicleIds.get(0));
                logSearchServiceImpl.addLogWithBrandAndColor(unBindEvent.getIpAddress(), message.toString(), "3",
                        "", vehicle[0], vehicle[1], unBindEvent.getUserName(), unBindEvent.getOrgId());
            } else {
                logSearchServiceImpl.addLogByUserNameAndOrgId(unBindEvent.getIpAddress(), message.toString(), "3",
                        "batch", "????????????????????????", unBindEvent.getUserName(), unBindEvent.getOrgId());
            }
        }
    }

    @Override
    public List<RiskEventVehicleConfigForm> findAllRiskSetting() {
        return riskEventConfigDao.findRiskAllSetting();
    }

    @Override
    public RiskEventVehicleConfigForm findRiskEventConfigByMap(Map<String, Object> map) {
        return riskEventConfigDao.findRiskEventConfigByMap(map);
    }

    private void sendMessage(List<String> parameterList) {
        if (parameterList.size() > 0) {
            ZMQFencePub.pubAdasRiskParam(JSON.toJSONString(parameterList));
        }
    }
}
