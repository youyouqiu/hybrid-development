package com.zw.platform.service.generalCargoReport.impl;

import com.cb.platform.domain.OffLineExportBusinessId;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.domain.generalCargoReport.WorkHandOverRecordDO;
import com.zw.platform.dto.reportManagement.WorkHandOverRecordQuery;
import com.zw.platform.service.generalCargoReport.WorkHandoverRecordService;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.util.common.AlarmTypeUtil;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import com.zw.talkback.common.ControllerTemplate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 值班交接班记录报表
 * @author CJY
 */
@Service
public class WorkHandoverRecordServiceImpl implements WorkHandoverRecordService {

    @Autowired
    private UserService userService;

    @Autowired
    private OfflineExportService exportService;

    @Override
    public JsonResultBean getWorkHandOverRecord(WorkHandOverRecordQuery query) throws Exception {
        String orgIds = query.getOrgIds();
        String startTime = query.getStartTime();
        String endTime = query.getEndTime();
        if (StringUtils.isBlank(orgIds) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
        }
        Set<String> filterOrgIds = userService.fuzzySearchFilterOrgIds(query.getSimpleQueryParam(), orgIds);
        if (CollectionUtils.isEmpty(filterOrgIds)) {
            return new JsonResultBean(Collections.emptyList());
        }
        Map<String, String> params = new HashMap<>(16);
        params.put("organizationIds", String.join(",", filterOrgIds));
        params.put("startTime", DateUtil.getStringToString(startTime, null, DateUtil.DATE_FORMAT));
        params.put("endTime", DateUtil.getStringToString(endTime, null, DateUtil.DATE_FORMAT));
        params.put("alarmType", StringUtils.join(AlarmTypeUtil.getAllAlarmType(), ","));
        String paasResult = HttpClientUtil.send(PaasCloudUrlEnum.SHIFT_HANDOVER_RECORD_REPORT, params);
        List<WorkHandOverRecordDO> resultListData =
            PaasCloudUrlUtil.getResultListData(paasResult, WorkHandOverRecordDO.class);
        return new JsonResultBean(resultListData);
    }

    @Override
    public JsonResultBean exportWorkHandOverRecord(WorkHandOverRecordQuery query) {
        String orgIds = query.getOrgIds();
        String startTime = query.getStartTime();
        String endTime = query.getEndTime();
        if (StringUtils.isBlank(orgIds) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
        }
        Set<String> filterOrgIds = userService.fuzzySearchFilterOrgIds(query.getSimpleQueryParam(), orgIds);
        if (CollectionUtils.isEmpty(filterOrgIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "模糊搜索后无数据");
        }
        TreeMap<String, String> params = new TreeMap<>();
        params.put("organizationIds", String.join(",", filterOrgIds));
        params.put("startTime", DateUtil.getStringToString(startTime, null, DateUtil.DATE_FORMAT));
        params.put("endTime", DateUtil.getStringToString(endTime, null, DateUtil.DATE_FORMAT));
        params.put("alarmType", StringUtils.join(AlarmTypeUtil.getAllAlarmType(), ","));
        String fileName = "道路运输车辆动态监控值班（交接班）记录表" + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo instance = OfflineExportInfo.getInstance("值班交接班记录表", fileName + ".xls");
        instance.assembleCondition(params, OffLineExportBusinessId.SHIFT_HANDOVER_RECORD_REPORT);
        return ControllerTemplate.addExportOffline(exportService, instance, "导出列表异常");
    }

}
