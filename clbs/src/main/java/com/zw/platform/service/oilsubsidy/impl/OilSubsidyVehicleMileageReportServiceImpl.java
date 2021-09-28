package com.zw.platform.service.oilsubsidy.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.MileageStatisticInfo;
import com.zw.platform.basic.constant.DateFormatKey;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.util.MathUtil;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.oilsubsidy.mileagereport.OilSubsidyVehicleMileMonthVO;
import com.zw.platform.service.oilsubsidy.OilSubsidyVehicleMileageReportService;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author XK
 */
@Service
public class OilSubsidyVehicleMileageReportServiceImpl implements OilSubsidyVehicleMileageReportService {

    @Autowired
    private UserService userService;

    @Override
    public List<OilSubsidyVehicleMileMonthVO> getVehicleMileMonths(Collection<String> vehicleIds, String month)
        throws Exception {
        //先删除缓存里面的redis缓存
        deleteFromRedis(userService.getCurrentUserUuid());
        if (CollectionUtils.isEmpty(vehicleIds) || StringUtils.isBlank(month)) {
            return new ArrayList<>();
        }

        Calendar monthCalendar = Calendar.getInstance();
        monthCalendar.setTime(Objects.requireNonNull(DateUtil.getStringToDate(month + "-01 00:00:00", null)));
        //获取对应月份的里程统计数据
        month = DateUtil.formatDate(month, DateFormatKey.YYYY_MM, DateFormatKey.YYYYMM);
        List<MileageStatisticInfo> mileList = getMileageDataByDay(vehicleIds, month);
        if (CollectionUtils.isEmpty(mileList)) {
            return new ArrayList<>();
        }

        int monthLastDay = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        Long monthFirstDay = monthCalendar.getTime().getTime() / 1000;
        Map<String, Double[]> monitorMileMap = new HashMap<>(CommonUtil.ofMapCapacity(vehicleIds.size()));
        for (MileageStatisticInfo mileStat : mileList) {
            String monitorId = UuidUtils.getUUIDStrFromBytes(mileStat.getMonitorIdHBase());
            Double[] monthMiles = monitorMileMap.getOrDefault(monitorId, new Double[monthLastDay]);
            int index = DateUtil.getTwoTimeDifference(monthFirstDay, mileStat.getDay());
            Double dayMile = getDayMile(mileStat);
            monthMiles[index] = dayMile;
            monitorMileMap.put(monitorId, monthMiles);
        }

        //获取监控对象的名称和组织
        Set<String> monitorSet = monitorMileMap.keySet();
        Map<String, BindDTO> monitorMap = MonitorUtils.getBindDTOMap(monitorSet, "name", "orgName");

        List<OilSubsidyVehicleMileMonthVO> mileMonthVOList = new ArrayList<>();
        for (String monitorId : monitorSet) {
            OilSubsidyVehicleMileMonthVO mileMonthVO = new OilSubsidyVehicleMileMonthVO();
            Double[] monthMiles = monitorMileMap.get(monitorId);
            mileMonthVO.setDays(monthMiles);
            mileMonthVO.setEnterpriseName(monitorMap.get(monitorId).getOrgName());
            mileMonthVO.setVehicleBrandNumber(monitorMap.get(monitorId).getName());
            Double totalMile = 0.0;
            for (Double mile : monthMiles) {
                if (Objects.nonNull(mile)) {
                    totalMile = MathUtil.add(totalMile, mile);
                }
            }
            mileMonthVO.setMonthReport(totalMile);
            mileMonthVOList.add(mileMonthVO);
        }

        addToRedis(mileMonthVOList);
        return mileMonthVOList;
    }

    private List<MileageStatisticInfo> getMileageDataByDay(Collection<String> vehicleIds, String month) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleIds", JSON.toJSONString(vehicleIds));
        params.put("month", month);
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_MILEAGE_DATA_BY_DAY, params);
        return PaasCloudUrlUtil.getResultListData(str, MileageStatisticInfo.class);
    }

    private void deleteFromRedis(String userId) {
        RedisKey redisKey = HistoryRedisKeyEnum.USER_OIL_SUBSIDY_VEHICLE_MILE_MONTH.of(userId);
        if (RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.delete(redisKey);
        }
    }

    private void addToRedis(List<OilSubsidyVehicleMileMonthVO> mileMonthVOList) {
        String userId = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_OIL_SUBSIDY_VEHICLE_MILE_MONTH.of(userId);
        if (CollectionUtils.isNotEmpty(mileMonthVOList)) {
            RedisHelper.delete(redisKey);
            RedisHelper.addToList(redisKey, mileMonthVOList);
        }
    }

    private Double getDayMile(MileageStatisticInfo mileStat) {
        // 由于此处可能存在历史数据, 3.8.2版本如果sensorFlag = 0, 则表示未绑定传感器milage=gpsMile,
        // sensorFlag =1则表示绑定了里程传感器milage=传感器里程
        Integer sensorFlag = mileStat.getSensorFlag();
        Double mileage = mileStat.getMileage();
        Double gpsMile = mileStat.getGpsMile();
        return getMile(sensorFlag, mileage, gpsMile);
    }

    @Override
    public void exportVehicleMonth(String title, int type, HttpServletResponse res) throws IOException {
        String userId = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_OIL_SUBSIDY_VEHICLE_MILE_MONTH.of(userId);
        List<OilSubsidyVehicleMileMonthVO> vehicleMileMonthList = new ArrayList<>();
        if (RedisHelper.isContainsKey(redisKey)) {
            List<String> list = RedisHelper.getList(redisKey);
            vehicleMileMonthList = list.stream().map(o -> JSONObject.parseObject(o, OilSubsidyVehicleMileMonthVO.class))
                .collect(Collectors.toList());
        }
        if (vehicleMileMonthList.size() == 0) {
            return;
        }
        List<String> headList = new ArrayList<String>();
        List<String> requiredList = new ArrayList<String>();
        List<Object> exportList;
        headList.add("对接码组织");
        headList.add("车牌号");
        for (int i = 1; i <= vehicleMileMonthList.get(0).getDays().length; i++) {
            headList.add(String.valueOf(i));
        }
        headList.add("合计");
        Map<String, String[]> selectMap = new HashMap<>();
        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row;

        for (OilSubsidyVehicleMileMonthVO oilSubsidyVehicleMileMonthVO : vehicleMileMonthList) {
            exportList = new ArrayList<>();
            exportList.add(oilSubsidyVehicleMileMonthVO.getEnterpriseName());
            exportList.add(oilSubsidyVehicleMileMonthVO.getVehicleBrandNumber());
            exportList.addAll(Arrays.asList(oilSubsidyVehicleMileMonthVO.getDays()));
            exportList.add(oilSubsidyVehicleMileMonthVO.getMonthReport());
            row = export.addRow();
            for (int x = 0; x < exportList.size(); x++) {
                export.addCell(row, x, exportList.get(x));
            }
        }
        OutputStream out;
        out = res.getOutputStream();
        export.write(out);
        out.close();
    }

    @Override
    public Double getMile(Integer sensorFlag, Double mileage, Double gpsMile) {
        Double dayMile;

        if (Objects.equals(sensorFlag, 0)) {
            //未绑定传感器, gpsMile不存在, 但mileage存在,则使用mileage
            if (Objects.isNull(gpsMile) && Objects.nonNull(mileage)) {
                dayMile = mileage;
            } else {
                dayMile = Objects.isNull(gpsMile) ? 0.0 : gpsMile;
            }
        } else {
            if (Objects.isNull(mileage) && Objects.nonNull(gpsMile)) {
                dayMile = gpsMile;
            } else {
                dayMile = Objects.isNull(mileage) ? 0.0 : mileage;
            }
        }
        return dayMile;
    }
}
