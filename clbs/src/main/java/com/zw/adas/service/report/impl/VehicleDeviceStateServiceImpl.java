package com.zw.adas.service.report.impl;

import com.zw.adas.domain.report.paas.VehicleDeviceRunStatusDTO;
import com.zw.adas.domain.report.query.SingleVehicleStateQuery;
import com.zw.adas.domain.report.query.VehicleDeviceStateQuery;
import com.zw.adas.service.report.VehicleDeviceStateService;
import com.zw.platform.basic.constant.DateFormatKey;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.dto.paas.PaasCloudPageDTO;
import com.zw.platform.dto.paas.PaasCloudPageDataDTO;
import com.zw.platform.dto.paas.PaasCloudResultDTO;
import com.zw.platform.util.FuzzySearchUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudAlarmUrlEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 车辆与设备运行状态
 * @author zhangjuan
 */
@Slf4j
@Service
public class VehicleDeviceStateServiceImpl implements VehicleDeviceStateService {
    @Autowired
    private NewVehicleDao vehicleDao;

    @Override
    public PageGridBean getList(VehicleDeviceStateQuery query) throws Exception {
        Map<String, String> queryCondition = getQueryCondition(query, true);

        //到pass端获取车辆与设备的运行状态
        String queryResult = HttpClientUtil.send(PaasCloudAlarmUrlEnum.VEHICLE_DEVICE_STATE_URL, queryCondition);
        PaasCloudResultDTO<PaasCloudPageDataDTO<VehicleDeviceRunStatusDTO>> resultData =
            PaasCloudUrlUtil.pageResult(queryResult, VehicleDeviceRunStatusDTO.class);

        List<VehicleDeviceRunStatusDTO> vehicleStateList = resultData.getData().getItems();
        PaasCloudPageDTO pageInfo = resultData.getData().getPageInfo();
        if (CollectionUtils.isEmpty(vehicleStateList)) {
            return new PageGridBean();
        }
        // 时间格式进行装换
        for (VehicleDeviceRunStatusDTO vehicleState : vehicleStateList) {
            String gpsTime = vehicleState.getTime();
            if (StringUtils.isBlank(gpsTime)) {
                continue;
            }
            gpsTime = DateUtil.formatDate(gpsTime, DateFormatKey.YYYYMMDDHHMMSS, DateFormatKey.YYYY_MM_DD_HH_MM_SS);
            vehicleState.setTime(gpsTime);
        }
        return new PageGridBean(vehicleStateList, pageInfo);
    }

    @Override
    public Set<String> fuzzyVehicleByBrand(String orgId, String keyword) {
        RedisKey fuzzyKey = HistoryRedisKeyEnum.MONITOR_DEVICE_SIM_FUZZY.of();
        Set<String> vehicleIds = FuzzySearchUtil.scanBindMonitor(fuzzyKey, keyword, MonitorTypeEnum.VEHICLE.getType());
        if (CollectionUtils.isEmpty(vehicleIds)) {
            return null;
        }
        return vehicleDao.getByOrgId(orgId, vehicleIds);
    }

    @Override
    public Map<String, String> getQueryCondition(VehicleDeviceStateQuery query, boolean isPage) {
        Date date = query.getDate();
        String startTime = DateUtil.formatDate(date, DateFormatKey.YYYYMMDD) + "000000";
        String endTime = DateUtil.formatDate(date, DateFormatKey.YYYYMMDDHHMMSS);
        Map<String, String> condition = new HashMap<>(16);
        condition.put("organizationId", query.getOrgId());
        condition.put("startTime", startTime);
        condition.put("endTime", endTime);
        if (StringUtils.isNotBlank(query.getSimpleQueryParam())) {
            condition.put("fuzzyQueryParam", query.getSimpleQueryParam());
        }
        if (isPage) {
            condition.put("page", String.valueOf(query.getPage()));
            condition.put("pageSize", String.valueOf(query.getLength()));
        }
        return condition;
    }

    @Override
    public VehicleDeviceRunStatusDTO getSingleState(SingleVehicleStateQuery query) throws Exception {
        //封装查询条件
        Map<String, String> param = new HashMap<>(16);
        param.put("groupId", query.getOrgId());
        param.put("time", DateUtil.formatDate(query.getDate(), DateFormatKey.YYYYMMDDHHMMSS));
        param.put("dataSource", String.valueOf(query.getDataSource()));
        param.put("monitorId", query.getVehicleId());

        //到pass端获取车辆与终端运行状态数据
        String queryResult = HttpClientUtil.send(PaasCloudAlarmUrlEnum.SINGLE_VEHICLE_DEVICE_STATE_URL, param);
        VehicleDeviceRunStatusDTO result = PaasCloudUrlUtil.getResultData(queryResult, VehicleDeviceRunStatusDTO.class);
        String time =
            DateUtil.formatDate(result.getTime(), DateFormatKey.YYYYMMDDHHMMSS, DateFormatKey.YYYY_MM_DD_HH_MM_SS);
        result.setTime(time);
        return result;
    }

}
