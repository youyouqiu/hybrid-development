package com.zw.adas.service.equipmentrepair.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.zw.adas.domain.equipmentrepair.BatchConfirmRepairDTO;
import com.zw.adas.domain.equipmentrepair.BatchFinishRepairDTO;
import com.zw.adas.domain.equipmentrepair.ConfirmDeviceRepairDTO;
import com.zw.adas.domain.equipmentrepair.DeviceRepairDTO;
import com.zw.adas.domain.equipmentrepair.FinishDeviceRepairDTO;
import com.zw.adas.domain.equipmentrepair.paas.DeviceRequestRepairDTO;
import com.zw.adas.domain.equipmentrepair.paas.EquipmentRepairMsg;
import com.zw.adas.domain.equipmentrepair.paas.EquipmentRepairUpMsgBody;
import com.zw.adas.domain.equipmentrepair.query.DeviceRepairQuery;
import com.zw.adas.service.equipmentrepair.EquipmentRepairService;
import com.zw.platform.basic.constant.DateFormatKey;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.connectionparamsset_809.T809ForwardConfig;
import com.zw.platform.domain.netty.ServerParamList;
import com.zw.platform.dto.paas.PaasCloudPageDTO;
import com.zw.platform.dto.paas.PaasCloudPageDataDTO;
import com.zw.platform.dto.paas.PaasCloudResultDTO;
import com.zw.platform.repository.modules.ConnectionParamsConfigDao;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudAlarmUrlEnum;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t809.T809Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 设备维修上报逻辑处理
 * @author zhnagjuan
 */
@Slf4j
@Service
public class EquipmentRepairServiceImpl implements EquipmentRepairService, IpAddressService {
    /**
     * 支持维修上报的809协议，目前只支持黑标
     */
    private static final List<String> UP_REPORT_809_PROTOCOL_CODE = Arrays.asList("1013");

    @Autowired
    private ConnectionParamsConfigDao paramsConfigDao;

    @Autowired
    private ServerParamList serverParamList;

    @Autowired
    private LogSearchService logService;

    @Override
    public PageGridBean getList(DeviceRepairQuery query) throws Exception {
        //构造查询数据的条件
        Map<String, String> param = query.getQueryCondition(true);
        //查询设备上报维修记录
        String resultStr = HttpClientUtil.send(PaasCloudAlarmUrlEnum.DEVICE_REPAIR_PAGE_URL, param);
        PaasCloudResultDTO<PaasCloudPageDataDTO<DeviceRequestRepairDTO>> resultData =
            PaasCloudUrlUtil.pageResult(resultStr, DeviceRequestRepairDTO.class);

        PaasCloudPageDTO pageInfo = resultData.getData().getPageInfo();
        List<DeviceRequestRepairDTO> repairList = resultData.getData().getItems();
        if (CollectionUtils.isEmpty(repairList)) {
            return new PageGridBean();
        }
        return new PageGridBean(getResult(repairList), pageInfo);
    }

    private List<DeviceRepairDTO> getResult(List<DeviceRequestRepairDTO> repairList) throws Exception {
        List<DeviceRepairDTO> deviceRepairList = new ArrayList<>();
        for (DeviceRequestRepairDTO repairDTO : repairList) {
            DeviceRepairDTO deviceRepairDTO = new DeviceRepairDTO(repairDTO);

            deviceRepairList.add(deviceRepairDTO);
        }

        return deviceRepairList;

    }

    @Override
    public DeviceRepairDTO getByPrimaryKey(String primaryKey) throws Exception {
        List<DeviceRepairDTO> repairList = getByPrimaryKeys(Collections.singletonList(primaryKey));
        return CollectionUtils.isEmpty(repairList) ? null : repairList.get(0);
    }

    @Override
    public List<DeviceRepairDTO> getByPrimaryKeys(Collection<String> primaryKeys) throws Exception {
        //封装参数
        Map<String, String> param = ImmutableMap.of("primaryKeys", StringUtils.join(primaryKeys, ","));
        //进行查询
        String resultStr = HttpClientUtil.send(PaasCloudAlarmUrlEnum.DEVICE_REPAIR_LIST_URL, param);
        List<DeviceRequestRepairDTO> resultList =
            PaasCloudUrlUtil.getResultListData(resultStr, DeviceRequestRepairDTO.class);
        if (CollectionUtils.isEmpty(resultList)) {
            return new ArrayList<>();
        }

        //进行返回结果封装
        return getResult(resultList);
    }

    @Override
    public boolean confirm(ConfirmDeviceRepairDTO confirmDTO) throws Exception {
        DeviceRepairDTO repairDTO = getByPrimaryKey(confirmDTO.getPrimaryKey());
        if (Objects.isNull(repairDTO)) {
            throw new BusinessException("该条维修上报记录不存在");
        }

        //封装确认参数
        Map<String, String> param = analysisPrimaryKey(confirmDTO.getPrimaryKey());
        param.put("confirmStatus", String.valueOf(confirmDTO.getConfirmStatus()));
        param.put("remark", confirmDTO.getRemark());

        //确认信息同步到PaaS端
        String resultStr = HttpClientUtil.send(PaasCloudAlarmUrlEnum.CONFIRM_DEVICE_REPAIR_URL, param);
        boolean isSuccess = PaasCloudUrlUtil.getSuccess(resultStr);
        if (!isSuccess) {
            log.error("{}到PaaS端确认设备维修失败", confirmDTO.getPrimaryKey());
            return false;
        }

        //若是确认，通知上级平台，误报不进行上报
        if (Objects.equals(confirmDTO.getConfirmStatus(), 0)) {
            send809Msg(Collections.singletonList(repairDTO), ConstantUtil.T809_UP_EXG_MSG_REPAIR_TERMINAL);
        }
        String monitorName = repairDTO.getMonitorName();
        String confirmStatus = Objects.equals(confirmDTO.getConfirmStatus(), 0) ? "确认" : "误报";
        String msg = String.format("设备报修：已确认%s报修单为%s", monitorName, confirmStatus);
        logService.addLog(getIpAddress(), msg, "3", "", monitorName, String.valueOf(repairDTO.getPlateColor()));
        return true;
    }

    @Override
    public boolean finish(FinishDeviceRepairDTO finishDTO) throws Exception {
        //查询维修上报记录
        DeviceRepairDTO repairDTO = getByPrimaryKey(finishDTO.getPrimaryKey());
        if (Objects.isNull(repairDTO)) {
            throw new BusinessException("该条维修上报记录不存在");
        }

        //组装请求参数
        Map<String, String> param = analysisPrimaryKey(finishDTO.getPrimaryKey());
        String repairDate =
            DateUtil.formatDate(finishDTO.getRepairDate(), DateFormatKey.YYYY_MM_DD, DateFormatKey.YYYYMMDD);
        param.put("repairDate", repairDate);
        param.put("remark", finishDTO.getRemark());

        //到PaaS端进行确认更新状态
        String resultStr = HttpClientUtil.send(PaasCloudAlarmUrlEnum.FINISH_DEVICE_REPAIR_URL, param);
        if (!PaasCloudUrlUtil.getSuccess(resultStr)) {
            log.error("{}到PaaS端更新设备维修完成失败", finishDTO.getPrimaryKey());
            return false;
        }
        repairDTO.setRepairDate(finishDTO.getRepairDate());

        //通知上级平台
        send809Msg(Collections.singletonList(repairDTO), ConstantUtil.T809_UP_EXG_MSG_REPAIR_FINISH);

        //记录日志
        String monitorName = repairDTO.getMonitorName();
        String msg = String.format("设备报修：完成%s报修单", monitorName);
        logService.addLog(getIpAddress(), msg, "3", "", monitorName, String.valueOf(repairDTO.getPlateColor()));
        return true;
    }

    @Override
    public boolean batchConfirm(BatchConfirmRepairDTO batchConfirmDTO) throws Exception {
        String[] primaryKeyArr = batchConfirmDTO.getPrimaryKeys().split(",");
        List<String> primaryKeys = new ArrayList<>(Arrays.asList(primaryKeyArr));
        List<DeviceRepairDTO> records = getByPrimaryKeys(primaryKeys);
        if (CollectionUtils.isEmpty(records)) {
            throw new BusinessException("未查询到维修上报记录");
        }

        Map<String, String> param = new HashMap<>(16);
        param.put("primaryKeys", StringUtils.join(batchConfirmDTO.getPrimaryKeys(), ","));
        param.put("confirmStatus", String.valueOf(batchConfirmDTO.getConfirmStatus()));

        //到PaaS端进行状态更改
        String resultStr = HttpClientUtil.send(PaasCloudAlarmUrlEnum.BATCH_CONFIRM_DEVICE_REPAIR_URL, param);
        if (!PaasCloudUrlUtil.getSuccess(resultStr)) {
            log.error("到PaaS端批量确认设备维修失败");
            return false;
        }
        //若是确认报修，信息上报到上级平台
        if (Objects.equals(batchConfirmDTO.getConfirmStatus(), 0)) {
            send809Msg(records, ConstantUtil.T809_UP_EXG_MSG_REPAIR_TERMINAL);
        }

        String confirmStatus = Objects.equals(batchConfirmDTO.getConfirmStatus(), 0) ? "确认" : "误报";
        Set<String> monitorNames =
            records.stream().map(DeviceRepairDTO::getMonitorName).distinct().collect(Collectors.toSet());
        String message = "设备报修:批量确认" + StringUtils.join(monitorNames, ",") + "的报修单状态为" + confirmStatus;
        logService.addLog(getIpAddress(), message, "3", "", "", "");
        return true;
    }

    @Override
    public boolean batchFinish(BatchFinishRepairDTO batchFinishDTO) throws Exception {
        String[] primaryKeyArr = batchFinishDTO.getPrimaryKeys().split(",");
        List<String> primaryKeys =
            Arrays.stream(primaryKeyArr).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<DeviceRepairDTO> records = getByPrimaryKeys(primaryKeys);
        if (CollectionUtils.isEmpty(records)) {
            throw new BusinessException("未查询到维修上报记录");
        }
        Map<String, String> param = new HashMap<>(16);
        param.put("primaryKeys", StringUtils.join(batchFinishDTO.getPrimaryKeys(), ","));
        String repairDate =
            DateUtil.formatDate(batchFinishDTO.getRepairDate(), DateFormatKey.YYYY_MM_DD, DateFormatKey.YYYYMMDD);
        param.put("repairDate", repairDate);

        //到PaaS端进行状态更改
        String resultStr = HttpClientUtil.send(PaasCloudAlarmUrlEnum.BATCH_FINISH_DEVICE_REPAIR_URL, param);
        if (!PaasCloudUrlUtil.getSuccess(resultStr)) {
            log.error("到PaaS端批量完成设备报修失败");
            return false;
        }
        Set<String> monitorNames = new HashSet<>();
        records.forEach(repairDTO -> {
            repairDTO.setRepairDate(batchFinishDTO.getRepairDate());
            monitorNames.add(repairDTO.getMonitorName());
        });
        send809Msg(records, ConstantUtil.T809_UP_EXG_MSG_REPAIR_FINISH);
        String message = "设备报修:批量完成" + StringUtils.join(monitorNames, ",") + "的报修单";
        logService.addLog(getIpAddress(), message, "3", "", "", "");
        return true;
    }

    /**
     * 上报809信息
     * @param records 设备维修记录
     * @param msgId   0x1211 上报维修信息  0x1212 上报维修完成信息
     */
    private void send809Msg(List<DeviceRepairDTO> records, int msgId) {
        //获取报修记录涉及到的报修车辆
        Set<String> monitorIds =
            records.stream().map(DeviceRepairDTO::getMonitorId).distinct().collect(Collectors.toSet());

        //获取车辆关联到的上级平台，目前只上报协议是黑标809-2019的上级平台
        List<T809ForwardConfig> configs =
            paramsConfigDao.getByMonitorIdAndProtocol(monitorIds, UP_REPORT_809_PROTOCOL_CODE);
        if (configs.isEmpty()) {
            return;
        }

        Map<String, List<T809ForwardConfig>> monitorParamMap =
            configs.stream().collect(Collectors.groupingBy(T809ForwardConfig::getId));
        for (DeviceRepairDTO repairDTO : records) {
            List<T809ForwardConfig> monitorParams = monitorParamMap.get(repairDTO.getMonitorId());
            if (CollectionUtils.isEmpty(monitorParams)) {
                continue;
            }
            for (T809ForwardConfig t809Config : monitorParams) {
                send809Msg(repairDTO, t809Config, msgId);
            }
        }
    }

    private void send809Msg(DeviceRepairDTO repairDTO, T809ForwardConfig t809Config, int dataType) {
        EquipmentRepairMsg upMsg = new EquipmentRepairMsg(repairDTO);
        EquipmentRepairUpMsgBody upMsgBody = new EquipmentRepairUpMsgBody();
        upMsgBody.setDataType(dataType);
        upMsgBody.setVehicleNo(repairDTO.getMonitorName());
        upMsgBody.setVehicleColor(repairDTO.getPlateColor());
        upMsgBody.setSourceDataType(dataType);
        int msgId = ConstantUtil.T809_UP_EXG_MSG;
        upMsgBody.setSourceMsgSn(msgId);
        upMsgBody.setData(upMsg);
        T809Message t809Message =
            MsgUtil.getT809Message(msgId, t809Config.getPlantFormIp(), t809Config.getPlatFormCenterId(), upMsgBody);
        Message message = MsgUtil.getMsg(msgId, t809Message).assembleDesc809(t809Config.getPlantFormId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
        log.info("设备维修上报信息确认或完成上报给上级平台的消息:{}", JSON.toJSONString(message));
    }

    private Map<String, String> analysisPrimaryKey(String primaryKey) {
        Map<String, String> param = new HashMap<>(16);
        String[] keys = primaryKey.split("_");
        param.put("groupId", keys[0]);
        param.put("requestRepairTime", keys[1]);
        param.put("malfunctionType", keys[2]);
        param.put("monitorId", keys[3]);
        return param;
    }

}
