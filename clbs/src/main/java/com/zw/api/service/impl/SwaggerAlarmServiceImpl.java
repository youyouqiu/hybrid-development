package com.zw.api.service.impl;

import com.alibaba.fastjson.JSON;
import com.zw.api.domain.AlarmDO;
import com.zw.api.domain.AlarmInfo;
import com.zw.api.service.MonitorInfoService;
import com.zw.api.service.SwaggerAlarmService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SwaggerAlarmServiceImpl implements SwaggerAlarmService {
    @Autowired
    private MonitorInfoService monitorInfoService;

    private static final Pattern SPLITTER = Pattern.compile(",");

    @Override
    public List<AlarmInfo> findAlarms(String monitorName, LocalDateTime startTime, LocalDateTime stopTime, int type) {
        final String[] monitorNames = SPLITTER.split(monitorName);
        if (monitorNames.length > 50) {
            throw new RuntimeException("监控对象数量超过限制");
        }
        Set<String> monitorIds = monitorInfoService.getMonitorIdByName(Arrays.asList(monitorNames));
        if (CollectionUtils.isEmpty(monitorIds)) {
            throw new RuntimeException("监控对象不存在");
        }
        long start = Date8Utils.getLongTime(startTime);
        long stop = Date8Utils.getLongTime(stopTime);

        final List<AlarmDO> alarms = this.listAlarmShortInfo(monitorIds, start, stop, type);
        return alarms.stream().map(AlarmInfo::fromAlarmDO).collect(Collectors.toList());
    }

    private List<AlarmDO> listAlarmShortInfo(Collection<String> monitorIds, long start, long stop, int type) {
        Map<String, String> params = new HashMap<>(8);
        params.put("monitorIds", JSON.toJSONString(monitorIds));
        params.put("startTime", String.valueOf(start));
        params.put("endTime", String.valueOf(stop));
        params.put("type", String.valueOf(type));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_ALARM_SHORT_INFO, params);
        return PaasCloudUrlUtil.getResultListData(str, AlarmDO.class);
    }

}
