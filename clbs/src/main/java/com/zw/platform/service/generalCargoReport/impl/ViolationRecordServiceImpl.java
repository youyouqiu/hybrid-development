package com.zw.platform.service.generalCargoReport.impl;

import com.cb.platform.domain.OffLineExportBusinessId;
import com.google.common.collect.Lists;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.domain.generalCargoReport.ViolationRecordDO;
import com.zw.platform.domain.generalCargoReport.ViolationRecordQuery;
import com.zw.platform.service.generalCargoReport.ViolationRecordService;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.util.common.AlarmTypeUtil;
import com.zw.platform.util.common.CargoCommonUtils;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import com.zw.talkback.common.ControllerTemplate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/***
 @Author zhengjc
 @Date 2019/9/4 14:54
 @Description 违章处置记录表
 @version 1.0
 **/
@Service
public class ViolationRecordServiceImpl implements ViolationRecordService {

    private static final List<Integer> ALARM_TYPES = new ArrayList<>();

    @Autowired
    private NewConfigDao newConfigDao;

    @Autowired
    private OfflineExportService exportService;

    @Value("${maxRecorder:-1}")
    private int maxRecorder;

    @PostConstruct
    private void initAlarmTypes() {
        Set<String> deviceAlarmTypes = AlarmTypeUtil.alarmMap.keySet();
        for (String deviceAlarmType : deviceAlarmTypes) {
            ALARM_TYPES.add(Integer.parseInt(deviceAlarmType));
        }
    }

    @Override
    public JsonResultBean getViolationRecords(ViolationRecordQuery query) throws Exception {
        String orgId = query.getOrgId();
        String startTime = query.getStartTime();
        String endTime = query.getEndTime();
        if (StringUtils.isBlank(orgId) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
        }
        Set<String> groupCargoVidSet = CargoCommonUtils.getGroupCargoVids(orgId.split(","));
        if (CollectionUtils.isEmpty(groupCargoVidSet)) {
            return new JsonResultBean(Collections.emptyList());
        }
        String simpleQueryParam = query.getSimpleQueryParam();
        Set<String> fuzzyMoIds = null;
        if (StringUtils.isNotBlank(simpleQueryParam)) {
            fuzzyMoIds = newConfigDao.getMoIdsByFuzzyMoName(simpleQueryParam);
            fuzzyMoIds = fuzzyMoIds.stream().filter(groupCargoVidSet::contains).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(fuzzyMoIds)) {
                return new JsonResultBean(Collections.emptyList());
            }
        }

        Map<String, String> params = new HashMap<>(16);
        params.put("organizationId", orgId);
        params.put("startTime", DateUtil.getStringToString(startTime, null, DateUtil.DATE_FORMAT));
        params.put("endTime", DateUtil.getStringToString(endTime, null, DateUtil.DATE_FORMAT));
        params.put("alarmTypes", StringUtils.join(ALARM_TYPES, ","));
        params.put("maxRecorder", String.valueOf(maxRecorder));
        if (CollectionUtils.isNotEmpty(fuzzyMoIds)) {
            params.put("fuzzyMonitorIds", String.join(",", fuzzyMoIds));
        }
        String paasResult = HttpClientUtil.send(PaasCloudUrlEnum.VIOLATION_RECORD_REPORT, params);
        List<ViolationRecordDO> resultListData =
            PaasCloudUrlUtil.getResultListData(paasResult, ViolationRecordDO.class);
        if (CollectionUtils.isEmpty(resultListData)) {
            return new JsonResultBean(Collections.emptyList());
        }
        List<String> moIds =
            resultListData.stream().map(ViolationRecordDO::getMonitorId).filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(moIds, Lists.newArrayList("orgName"));
        for (ViolationRecordDO resultListDatum : resultListData) {
            String monitorId = resultListDatum.getMonitorId();
            if (StringUtils.isNotBlank(monitorId) && bindInfoMap.containsKey(monitorId)) {
                resultListDatum.setOrgName(bindInfoMap.get(monitorId).getOrgName());
            }
            resultListDatum.setTimeStr(DateUtil
                .getStringToString(resultListDatum.getTime(), DateUtil.DATE_FORMAT_SSS, DateUtil.DATE_HH_MM_SS));
        }
        return new JsonResultBean(resultListData);
    }

    @Override
    public JsonResultBean exportViolationRecords(ViolationRecordQuery query) {
        String orgId = query.getOrgId();
        String startTime = query.getStartTime();
        String endTime = query.getEndTime();
        if (StringUtils.isBlank(orgId) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
        }
        Set<String> groupCargoVidSet = CargoCommonUtils.getGroupCargoVids(orgId.split(","));
        if (CollectionUtils.isEmpty(groupCargoVidSet)) {
            return new JsonResultBean(JsonResultBean.FAULT, "企业下无货运车辆");
        }
        String simpleQueryParam = query.getSimpleQueryParam();
        Set<String> fuzzyMoIds = null;
        if (StringUtils.isNotBlank(simpleQueryParam)) {
            fuzzyMoIds = newConfigDao.getMoIdsByFuzzyMoName(simpleQueryParam);
            fuzzyMoIds = fuzzyMoIds.stream().filter(groupCargoVidSet::contains).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(fuzzyMoIds)) {
                return new JsonResultBean(JsonResultBean.FAULT, "模糊搜索后无数据");
            }
        }
        TreeMap<String, String> params = new TreeMap<>();
        params.put("organizationId", orgId);
        params.put("startTime", DateUtil.getStringToString(startTime, null, DateUtil.DATE_FORMAT));
        params.put("endTime", DateUtil.getStringToString(endTime, null, DateUtil.DATE_FORMAT));
        params.put("alarmTypes", StringUtils.join(ALARM_TYPES, ","));
        params.put("maxRecorder", String.valueOf(maxRecorder));
        if (CollectionUtils.isNotEmpty(fuzzyMoIds)) {
            params.put("fuzzyMonitorIds", String.join(",", fuzzyMoIds));
        }
        String fileName = "道路运输车辆动态监控违章记录表" + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo instance = OfflineExportInfo.getInstance("违章记录表", fileName + ".xls");
        instance.assembleCondition(params, OffLineExportBusinessId.VIOLATION_RECORD_REPORT);
        return ControllerTemplate.addExportOffline(exportService, instance, "导出列表异常");
    }

}
