package com.zw.platform.service.switching.impl;

import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.query.SensorConfigQuery;
import com.zw.platform.domain.systems.form.DirectiveForm;
import com.zw.platform.domain.vas.alram.OutputControl;
import com.zw.platform.domain.vas.alram.OutputControlSendInfo;
import com.zw.platform.dto.ouputcontrol.OutputControlDTO;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.vas.AlarmSettingDao;
import com.zw.platform.service.alarm.impl.AlarmSettingServiceImpl;
import com.zw.platform.service.monitoring.OrderService;
import com.zw.platform.service.redis.RedisVehicleService;
import com.zw.platform.service.switching.SwitchOutputService;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisSensorQuery;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.ws.common.PublicVariable;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/***
 @Author lijie
 @Date 2020/5/12 11:40
 @Description 输出控制模块
 @version 1.0
 **/
@Service
public class SwitchOutputServiceImpl implements SwitchOutputService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlarmSettingDao alarmSettingDao;

    @Autowired
    private RedisVehicleService redisVehicleService;

    @Autowired
    private UserService userService;

    @Autowired
    private ParameterDao parameterDao;

    @Override
    public JsonResultBean send8500(OutputControl outputControl) {
        OutputControlSendInfo outputControlSendInfo =
            AlarmSettingServiceImpl.getOutputControlSendInfo(Collections.singletonList(outputControl), false);
        orderService.outputControlBy19(outputControlSendInfo, outputControl.getVehicleId(), false);
        outputControl.setAutoFlag(0);
        outputControl.setCreateDataUsername(SystemHelper.getCurrentUsername());
        alarmSettingDao.addOutputControlSetting(outputControl);
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public JsonResultBean sendCloseOutputControl(String vehicleId, String protocolType) {
        List<OutputControl> re = alarmSettingDao.getVehicleOutputControlSetting(vehicleId);
        Set<OutputControl> outputControls = new HashSet<>(re);
        if (CollectionUtils.isNotEmpty(outputControls)) {
            OutputControlSendInfo outputControlSendInfo =
                AlarmSettingServiceImpl.getOutputControlSendInfo(outputControls, true);
            orderService.outputControlBy19(outputControlSendInfo, vehicleId, false);
            alarmSettingDao.updateVehicleOutputControlSetting(vehicleId);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public PageGridBean pageList(SensorConfigQuery query) throws Exception {
        String groupId = query.getGroupId();
        String assignmentId = query.getAssignmentId();
        String simpleQueryParam = query.getSimpleQueryParam();
        Integer protocol = query.getProtocol();
        Long start = query.getStart();
        Long limit = query.getLimit();
        RedisSensorQuery redisQuery = new RedisSensorQuery(groupId, assignmentId, simpleQueryParam, protocol);
        List<String> vehicleList = redisVehicleService.getVehicleByType(redisQuery, null);
        List<String> pageIdList = vehicleList.stream().skip(start).limit(limit).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(pageIdList)) {
            return new PageGridBean();
        }
        String paramType = "F3-0x8500-OutputControl";
        Map<String, List<DirectiveForm>> directiveFormsMap =
            parameterDao.getSendStatusList(pageIdList, paramType).stream()
                .collect(Collectors.groupingBy(DirectiveForm::getMonitorObjectId));
        Map<String, DirectiveForm> directiveFormMap = new HashMap<>(16);
        for (List<DirectiveForm> directiveFormList : directiveFormsMap.values()) {
            directiveFormList.stream().max(Comparator.comparing(DirectiveForm::getDownTime))
                .ifPresent(obj -> directiveFormMap.put(obj.getMonitorObjectId(), obj));
        }
        Page<OutputControlDTO> resultPageList = new Page<>(query.getPage().intValue(), limit.intValue(), false);
        resultPageList.setTotal(vehicleList.size());
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(pageIdList);
        userService.setObjectTypeName(bindInfoMap.values());
        for (String moId : pageIdList) {
            OutputControlDTO outputControlDTO = new OutputControlDTO();
            outputControlDTO.setMoId(moId);
            BindDTO bindDTO = bindInfoMap.get(moId);
            outputControlDTO.setMoName(bindDTO.getName());
            outputControlDTO.setOrgName(bindDTO.getOrgName());
            outputControlDTO.setVehicleType(bindDTO.getObjectTypeName());
            DirectiveForm directiveForm = directiveFormMap.get(moId);
            if (directiveForm != null) {
                outputControlDTO.setDownTimeStr(DateUtil.getDateToString(directiveForm.getDownTime(), null));
                // 下发状态： 0:参数已生效; 1:参数未生效; 2:参数消息有误; 3:参数不支持;  4:参数下发中;
                // 5:终端离线，未下发; 7:终端处理中; 8:终端接收失败;
                Integer status = directiveForm.getStatus();
                String parameterName = directiveForm.getParameterName();
                outputControlDTO.setStatusStr(parameterName + PublicVariable.SEND_STATUS.p2b(status));
            }
            resultPageList.add(outputControlDTO);
        }
        return new PageGridBean(query, resultPageList, true);
    }

}
