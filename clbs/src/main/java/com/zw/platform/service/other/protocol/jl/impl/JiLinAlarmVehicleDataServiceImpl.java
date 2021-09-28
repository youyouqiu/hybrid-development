package com.zw.platform.service.other.protocol.jl.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.Sets;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.other.protocol.jl.dto.AlarmVehicleDO;
import com.zw.platform.domain.other.protocol.jl.dto.AlarmVehicleDTO;
import com.zw.platform.domain.other.protocol.jl.query.AddVehicleAlarmRelieveInfo;
import com.zw.platform.domain.other.protocol.jl.query.AlarmVehiclePageReq;
import com.zw.platform.domain.other.protocol.jl.query.AlarmVehicleReq;
import com.zw.platform.domain.other.protocol.jl.query.SingleAlarmVehicleReq;
import com.zw.platform.domain.other.protocol.jl.xml.ResponseRootElement;
import com.zw.platform.domain.other.protocol.jl.xml.SuccessContentElement;
import com.zw.platform.repository.modules.ConnectionParamsConfigDao;
import com.zw.platform.repository.other.protocol.jl.AlarmVehicleDao;
import com.zw.platform.service.other.protocol.jl.JiLinAlarmVehicleDataService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MapUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.jl.JiLinConstant;
import com.zw.platform.util.jl.JiLinOrgExamineInterfaceBaseParamEnum;
import com.zw.platform.util.jl.JiLinOrgExamineUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.zw.platform.util.jl.JiLinConstant.MSG;
import static com.zw.platform.util.jl.JiLinConstant.RESULT;
import static com.zw.protocol.util.ProtocolTypeUtil.T809_JI_LIN_PROTOCOL_809_2013;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/12 14:15
 */
@Service
public class JiLinAlarmVehicleDataServiceImpl implements JiLinAlarmVehicleDataService {

    @Autowired
    private JiLinOrgExamineUtil jiLinOrgExamineUtil;

    @Autowired
    private AlarmVehicleDao alarmVehicleDao;

    @Autowired
    private ConnectionParamsConfigDao connectionParamsConfigDao;

    @Autowired
    private UserService userService;

    @Override
    public JsonResultBean insertAlarmUpload(AlarmVehicleReq vehicleReq) throws Exception {
        Set<String> monitorIds = vehicleReq.getMonitorIds();
        Integer alarmType = vehicleReq.getAlarmType();
        Integer alarmHandleStatus = vehicleReq.getAlarmHandleStatus();
        Long alarmStartTimeL = Long.parseLong(
            Objects.requireNonNull(DateUtil.getDateToString(vehicleReq.getAlarmStartTime(), DateUtil.DATE_FORMAT)));
        Long alarmEndTimeL = Long.parseLong(
            Objects.requireNonNull(DateUtil.getDateToString(vehicleReq.getAlarmEndTime(), DateUtil.DATE_FORMAT)));

        List<BindDTO> configLists = RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(monitorIds)).stream()
            .map(o -> MapUtil.mapToObj(o, BindDTO.class)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(configLists)) {
            return new JsonResultBean(JsonResultBean.FAULT, "车辆信息不存在!");
        }
        // 组装上传数据
        List<AddVehicleAlarmRelieveInfo> requestXmlList = configLists.stream().map(
            configInfo -> new AddVehicleAlarmRelieveInfo(configInfo.getName(),
                JiLinConstant.CLBS_COLOR_JILIN_TRANSLATOR.b2p(configInfo.getPlateColor()), alarmType, alarmStartTimeL,
                alarmEndTimeL, alarmHandleStatus)).collect(Collectors.toList());
        // 上传报警车辆
        ResponseRootElement<SuccessContentElement> response = jiLinOrgExamineUtil
            .sendExamineRequest(requestXmlList, JiLinOrgExamineInterfaceBaseParamEnum.ADD_VEHICLE_ALARM_RELIEVE_INFO,
                SuccessContentElement.class);
        JSONObject resultJson = JiLinOrgExamineUtil.upLoadResolverResult(response);
        if (alarmVehicleDao.insertBatchAlarmUploadRecord(assembleUploadRecord(configLists, resultJson, vehicleReq))) {
            return new JsonResultBean(resultJson);
        }
        return new JsonResultBean(JsonResultBean.FAULT, "保存报警上传记录失败!");
    }

    private List<AlarmVehicleDO> assembleUploadRecord(Collection<BindDTO> configLists, JSONObject resultJson,
        AlarmVehicleReq vehicleReq) {
        final Date uploadTime = new Date();
        final String operator = SystemHelper.getCurrentUsername();
        final int uploadState = resultJson.getInteger(RESULT);
        final String errorMsg = resultJson.getString(MSG);
        Date startTime = vehicleReq.getAlarmStartTime();
        Date endTime = vehicleReq.getAlarmEndTime();
        Integer alarmType = vehicleReq.getAlarmType();
        Integer alarmHandleStatus = vehicleReq.getAlarmHandleStatus();
        return configLists.stream().map(
            configInfo -> new AlarmVehicleDO(configInfo.getId(), configInfo.getName(), startTime, endTime,
                alarmType, alarmHandleStatus, configInfo.getPlateColor(), configInfo.getOrgName(), uploadTime,
                uploadState, operator, errorMsg)).collect(Collectors.toList());
    }

    @Override
    public JsonResultBean insertBatchAlarmUpload(List<SingleAlarmVehicleReq> vehicleReqList) throws Exception {
        List<String> vehicleIdList =
            vehicleReqList.stream().map(SingleAlarmVehicleReq::getMonitorId).collect(Collectors.toList());

        Map<String, BindDTO> configMap =
            RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(vehicleIdList)).stream()
                .map(o -> MapUtil.mapToObj(o, BindDTO.class))
                .collect(Collectors.toMap(BindDTO::getId, Function.identity()));
        // 组装上传数据
        List<AddVehicleAlarmRelieveInfo> requestXmlList = vehicleReqList.stream().map(req -> {
            String monitorId = req.getMonitorId();
            Long alarmStartTimeL = Long.parseLong(
                Objects.requireNonNull(DateUtil.getDateToString(req.getAlarmStartTime(), DateUtil.DATE_FORMAT)));
            Long alarmEndTimeL = Long.parseLong(
                Objects.requireNonNull(DateUtil.getDateToString(req.getAlarmEndTime(), DateUtil.DATE_FORMAT)));
            BindDTO configInfo = configMap.get(monitorId);
            return new AddVehicleAlarmRelieveInfo(configInfo.getName(),
                JiLinConstant.CLBS_COLOR_JILIN_TRANSLATOR.b2p(configInfo.getPlateColor()), req.getAlarmType(),
                alarmStartTimeL, alarmEndTimeL, req.getAlarmHandleStatus());
        }).collect(Collectors.toList());
        // 上传报警车辆
        ResponseRootElement<SuccessContentElement> response = jiLinOrgExamineUtil
            .sendExamineRequest(requestXmlList, JiLinOrgExamineInterfaceBaseParamEnum.ADD_VEHICLE_ALARM_RELIEVE_INFO,
                SuccessContentElement.class);
        JSONObject resultJson = JiLinOrgExamineUtil.upLoadResolverResult(response);
        if (alarmVehicleDao.insertBatchAlarmUploadRecord(assembleUploadRecord(vehicleReqList, configMap, resultJson))) {
            return new JsonResultBean(resultJson);
        }
        return new JsonResultBean(JsonResultBean.FAULT, "保存报警上传记录失败!");
    }

    private List<AlarmVehicleDO> assembleUploadRecord(List<SingleAlarmVehicleReq> vehicleReqList,
        Map<String, BindDTO> configInfoMap, JSONObject resultJson) {
        final Date uploadTime = new Date();
        final String operator = SystemHelper.getCurrentUsername();
        final int uploadState = resultJson.getInteger(RESULT);
        final String errorMsg = resultJson.getString(MSG);
        return vehicleReqList.stream().map(req -> {
            String monitorId = req.getMonitorId();
            BindDTO configInfo = configInfoMap.get(monitorId);
            return new AlarmVehicleDO(configInfo.getId(), configInfo.getName(), req.getAlarmStartTime(),
                req.getAlarmEndTime(), req.getAlarmType(), req.getAlarmHandleStatus(), configInfo.getPlateColor(),
                configInfo.getOrgName(), uploadTime, uploadState, operator, errorMsg);
        }).collect(Collectors.toList());
    }

    @Override
    public Page<AlarmVehicleDTO> listAlarmVehicle(AlarmVehiclePageReq alarmVehiclePageReq) {
        String monitorIdsStr = alarmVehiclePageReq.getMonitorIdsStr();
        if (StringUtils.isBlank(monitorIdsStr)) {
            Set<String> forwardVehicleIdList = connectionParamsConfigDao
                .findForwardVehiclesByProtocolType(Integer.valueOf(T809_JI_LIN_PROTOCOL_809_2013)).stream()
                .map(VehicleInfo::getId).collect(Collectors.toSet());
            Set<String> userAssignMonitorIds = userService.getCurrentUserMonitorIds();
            Set<String> newMonitorIds = Sets.intersection(forwardVehicleIdList, userAssignMonitorIds);
            if (CollectionUtils.isEmpty(newMonitorIds)) {
                return new Page<>();
            }
            alarmVehiclePageReq.setMonitorIds(newMonitorIds);
        } else {
            alarmVehiclePageReq.setMonitorIds(Arrays.stream(monitorIdsStr.split(",")).collect(Collectors.toSet()));
        }
        alarmVehiclePageReq.setSimpleQueryParam(StringUtil.fuzzyKeyword(alarmVehiclePageReq.getSimpleQueryParam()));
        alarmVehiclePageReq.setUploadStartDate(alarmVehiclePageReq.getUploadStartDate() + " 00:00:00");
        alarmVehiclePageReq.setUploadEndDate(alarmVehiclePageReq.getUploadEndDate() + " 23:59:59");
        final Page<AlarmVehicleDO> vehiclePage = PageHelperUtil
            .doSelect(alarmVehiclePageReq, () -> alarmVehicleDao.listAlarmVehicleUploadRecord(alarmVehiclePageReq));
        Page<AlarmVehicleDTO> resultPage =
            new Page<>(alarmVehiclePageReq.getPage().intValue(), alarmVehiclePageReq.getLimit().intValue(), false);
        vehiclePage.forEach(uploadRecord ->
            resultPage.add(new AlarmVehicleDTO(
                    uploadRecord.getId(),
                    uploadRecord.getMonitorId(),
                    uploadRecord.getMonitorName(),
                    DateUtil.getDateToString(uploadRecord.getStartTime(), null),
                    DateUtil.getDateToString(uploadRecord.getEndTime(), null),
                    JiLinConstant.ALARM_TYPE.b2p(uploadRecord.getAlarmType()),
                    JiLinConstant.ALARM_HANDLE_STATUS_TYPE.b2p(uploadRecord.getAlarmStatus()),
                    PlateColor.getNameOrBlankByCode(uploadRecord.getPlateColor()),
                    uploadRecord.getGroupName(),
                    DateUtil.getDateToString(uploadRecord.getUploadTime(), null),
                    Objects.equals(uploadRecord.getUploadState(), 1)
                        ? "上传成功" : "上传失败(" + uploadRecord.getErrorMsg() + ")",
                    uploadRecord.getOperator(),
                    uploadRecord.getErrorMsg())
            )
        );
        resultPage.setTotal(vehiclePage.getTotal());
        return resultPage;
    }

    @Override
    public void exportAlarmList(HttpServletResponse httpServletResponse, AlarmVehiclePageReq alarmVehiclePageReq)
        throws Exception {
        String monitorIdsStr = alarmVehiclePageReq.getMonitorIdsStr();
        if (StringUtils.isBlank(monitorIdsStr)) {
            Set<String> forwardVehicleIdList = connectionParamsConfigDao
                .findForwardVehiclesByProtocolType(Integer.valueOf(T809_JI_LIN_PROTOCOL_809_2013)).stream()
                .map(VehicleInfo::getId).collect(Collectors.toSet());
            Set<String> userAssignMonitorIds = userService.getCurrentUserMonitorIds();
            Set<String> newMonitorIds = Sets.intersection(forwardVehicleIdList, userAssignMonitorIds);
            if (CollectionUtils.isEmpty(newMonitorIds)) {
                ExportExcelUtil.export(new ExportExcelParam("", 1, new ArrayList<>(), AlarmVehicleDTO.class, null,
                    httpServletResponse.getOutputStream()));
                return;
            }
            alarmVehiclePageReq.setMonitorIds(newMonitorIds);
        } else {
            alarmVehiclePageReq.setMonitorIds(Arrays.stream(monitorIdsStr.split(",")).collect(Collectors.toSet()));
        }
        alarmVehiclePageReq.setSimpleQueryParam(StringUtil.fuzzyKeyword(alarmVehiclePageReq.getSimpleQueryParam()));
        alarmVehiclePageReq.setUploadStartDate(alarmVehiclePageReq.getUploadStartDate() + " 00:00:00");
        alarmVehiclePageReq.setUploadEndDate(alarmVehiclePageReq.getUploadEndDate() + " 23:59:59");
        List<AlarmVehicleDO> alarmUploadRecords = alarmVehicleDao.listAlarmVehicleUploadRecord(alarmVehiclePageReq);
        List<AlarmVehicleDTO> exportList = alarmUploadRecords.stream().map(uploadRecord ->
            new AlarmVehicleDTO(
                uploadRecord.getId(),
                uploadRecord.getMonitorId(),
                uploadRecord.getMonitorName(),
                DateUtil.getDateToString(uploadRecord.getStartTime(), null),
                DateUtil.getDateToString(uploadRecord.getEndTime(), null),
                JiLinConstant.ALARM_TYPE.b2p(uploadRecord.getAlarmType()),
                JiLinConstant.ALARM_HANDLE_STATUS_TYPE.b2p(uploadRecord.getAlarmStatus()),
                PlateColor.getNameOrBlankByCode(uploadRecord.getPlateColor()), uploadRecord.getGroupName(),
                DateUtil.getDateToString(uploadRecord.getUploadTime(), null),
                Objects.equals(uploadRecord.getUploadState(), 1) ? "上传成功" : "上传失败(" + uploadRecord.getErrorMsg() + ")",
                uploadRecord.getOperator(),
                uploadRecord.getErrorMsg())
        ).collect(Collectors.toList());
        ExportExcelUtil.export(new ExportExcelParam("", 1, exportList, AlarmVehicleDTO.class, null,
            httpServletResponse.getOutputStream()));
    }
}
