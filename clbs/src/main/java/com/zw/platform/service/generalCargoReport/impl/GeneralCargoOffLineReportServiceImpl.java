package com.zw.platform.service.generalCargoReport.impl;

import com.alibaba.fastjson.JSON;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.generalCargoReport.CargoOffLineReport;
import com.zw.platform.service.generalCargoReport.GeneralCargoOffLineReportService;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.msg.t808.body.LocationInfo;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

/**
 * @author Admin
 * 山东离线报表
 * @author XK
 */
@Service
public class GeneralCargoOffLineReportServiceImpl implements GeneralCargoOffLineReportService {
    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";

    private static final String FORMAT = "yyyyMMddHHmmss";

    @Autowired
    UserService userService;

    @Override
    public List<CargoOffLineReport> getList(Set<String> vehicleIds, Integer day) throws Exception {
        long nowTime = System.currentTimeMillis();
        long dayTime = day * 86400000L;
        Map<String, List<String>> locationMap = new HashMap<>();
        List<CargoOffLineReport> eligible = new ArrayList<>();
        //过滤调在线的车辆
        filterVehicleIds(vehicleIds);
        // 管道取出所以车辆最后一次位置的缓存信息，获取最后一次在线位置，计算离线时长
        Map<String, VehicleDTO> vehicleMap = MonitorUtils.getVehicleMap(vehicleIds);
        Map<String, Message> lastLocationMap = MonitorUtils.getLocationMap(vehicleIds);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        for (Map.Entry<String, Message> entry : lastLocationMap.entrySet()) {
            if (entry.getValue() == null || lastLocationMap.get(entry.getKey()) == null) {
                continue;
            }
            Message message = entry.getValue();
            T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
            LocationInfo msgBody =
                    JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);
            Date date = DateUtils.parseDate("20" + msgBody.getGpsTime(), FORMAT);
            long gpsTime = date.getTime();
            if (nowTime - gpsTime > dayTime) {
                //经度
                String longitude =
                    msgBody.getLongitude() != null ? String.valueOf(msgBody.getLongitude()) : "0.0";
                // 纬度
                String latitude =
                    msgBody.getLatitude() != null ? String.valueOf(msgBody.getLatitude()) : "0.0";
                CargoOffLineReport cargoOffLineReport = new CargoOffLineReport();
                cargoOffLineReport.setLastTime(sdf.format(date));
                cargoOffLineReport.setBrand(vehicleMap.get(entry.getKey()).getName());
                cargoOffLineReport.setGroupName(vehicleMap.get(entry.getKey()).getOrgName());
                if ("".equals(latitude) || "".equals(longitude) || "0".equals(latitude) || "0".equals(longitude)
                    || "0.0".equals(latitude) || "0.0".equals(longitude)) {
                    cargoOffLineReport.setLastLocation("未定位");
                } else {
                    buildLocationMap(locationMap, entry, longitude, latitude, cargoOffLineReport);
                }
                eligible.add(cargoOffLineReport);
            }
        }
        //逆地址解析
        Map<String, String> addressMap = AddressUtil.batchInverseAddressFromHBase(locationMap, true);

        for (CargoOffLineReport cargoOffLineReport : eligible) {
            if (cargoOffLineReport.getKey() != null) {
                String address = addressMap.get(cargoOffLineReport.getKey());
                cargoOffLineReport.setLastLocation(address);
            }
        }
        eligible.sort(comparing(CargoOffLineReport::getLastTime));
        return eligible;
    }


    private void buildLocationMap(Map<String, List<String>> locationMap, Map.Entry<String, Message> entry,
        String longitude, String latitude, CargoOffLineReport cargoOffLineReport) {
        if (longitude.length() >= 7) {
            longitude = longitude.substring(0, 7);
        }
        if (latitude.length() >= 6) {
            latitude = latitude.substring(0, 6);
        }
        float lon = Float.parseFloat(longitude);
        float lat = Float.parseFloat(latitude);
        String key = lon + "," + lat;
        List<String> monitorIds = locationMap.get(key);
        if (monitorIds == null) {
            monitorIds = new ArrayList<>();
        }
        monitorIds.add(entry.getKey());
        cargoOffLineReport.setKey(key);
        locationMap.put(key, monitorIds);
    }

    /**
     * 过滤掉在线的车辆ids
     */
    private void filterVehicleIds(Set<String> vehicleIds) {
        Set<String> set = new HashSet<>(RedisHelper.scanKeys(HistoryRedisKeyEnum.MONITOR_STATUS.of("*")));
        Set<String> set1 =
            set.stream().map(s -> s.substring(0, s.indexOf("-vehiclestatus"))).collect(Collectors.toSet());
        vehicleIds.removeAll(set1);
    }

    @Override
    public boolean export(String title, int type, HttpServletResponse res, List<CargoOffLineReport> offLineReport)
        throws Exception {

        return ExportExcelUtil.export(
            new ExportExcelParam(title, type, offLineReport, CargoOffLineReport.class, null, res.getOutputStream()));
    }

    @Override
    public List<CargoOffLineReport> getExportList(String simpleQueryParam, List<CargoOffLineReport> list) {
        List<CargoOffLineReport> cargoOffLineReports = new ArrayList<>();
        for (CargoOffLineReport cargoOffLineReport : list) {
            if (cargoOffLineReport.getBrand().contains(simpleQueryParam) || cargoOffLineReport.getGroupName()
                .contains(simpleQueryParam)) {
                cargoOffLineReports.add(cargoOffLineReport);
            }
        }
        return cargoOffLineReports;
    }
}
