package com.sx.platform.service.sxReportManagement.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.util.page.PassCloudResultBean;
import com.github.pagehelper.util.StringUtil;
import com.sx.platform.domain.sxReport.LocationQualifiedRateReport;
import com.sx.platform.service.sxReportManagement.LocationQualifiedRateReportService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhangsq
 * @date 2018/3/12 11:08
 */
@Service
public class LocationQualifiedRateReportServiceImpl implements LocationQualifiedRateReportService {

    @Autowired
    UserService userService;

    @Autowired
    OrganizationService organizationService;

    @Override
    public JsonResultBean getLocationQualifiedRateFromPaas(String band, String startTime, String endTime)
        throws Exception {
        List<LocationQualifiedRateReport> result = new ArrayList<>();
        Map<String, String> param = new HashMap<>();
        param.put("startTime", DateUtil.formatDate(startTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT));
        param.put("endTime", DateUtil.formatDate(endTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT));
        param.put("monitorIds", band);
        String sendResult = HttpClientUtil.send(PaasCloudUrlEnum.SX_LOCATION_QUALIFIED_URL, param);
        PassCloudResultBean passCloudResultBean = PassCloudResultBean.getDataInstance(sendResult);
        Object data = passCloudResultBean.getData();
        if (!passCloudResultBean.isSuccess()) {
            return new JsonResultBean(JsonResultBean.FAULT, passCloudResultBean.getMessage());
        }
        if (Objects.nonNull(data)) {
            List<String> monitorIds = Arrays.asList(band.split(","));
            Map<String, VehicleDTO> vehicleMap = MonitorUtils.getVehicleMap(monitorIds);
            JSONArray jsonArray = JSONObject.parseArray(data.toString());
            if (CollectionUtils.isNotEmpty(jsonArray)) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    LocationQualifiedRateReport report = new LocationQualifiedRateReport();
                    report.setPlateNumber(object.getString("monitorName"));
                    report.setAssignmentName(object.getString("assignmentName"));
                    report.setQualifiedRate(object.getString("qualifiedRate"));
                    report.setTotalCount(object.getLong("totalCount"));
                    report.setUnqualifiedCount(object.getLong("unqualifiedCount"));
                    report.setVehType(object.getString("objectType"));
                    String monitorId = object.getString("monitorId");
                    VehicleDTO vehicleDTO = vehicleMap.get(monitorId);
                    report.setGroupName(vehicleDTO.getOrgName());
                    report.setPlateColor(PlateColor.getNameOrBlankByCode(vehicleDTO.getPlateColor()));
                    result.add(report);
                }
            }
        }
        String userId = userService.getCurrentUserInfo().getId().toString();
        RedisKey redisKey = HistoryRedisKeyEnum.SX_LOCATION_QUALIFIED_RATE_REPORT_INFORMATION.of(userId);
        // 再次查询前删除 key
        RedisHelper.delete(redisKey);
        // 获取组装数据存入redis管道
        RedisHelper.addToList(redisKey, result);
        return new JsonResultBean(result);
    }

    /**
     * 根据车辆Id分组排序
     * @param positionals
     * @return
     */
    private Map<String, List<Positional>> sort(List<Positional> positionals) {
        Map<String, List<Positional>> listMap = new HashMap<>();
        if (positionals.size() > 0) {
            List<Positional> positionals1i = new ArrayList<>();
            for (int i = 0; i < positionals.size(); i++) {
                Positional positional = positionals.get(i);
                if (positionals.size() == 1) {
                    listMap.put(UuidUtils.getUUIDFromBytes(positional.getVehicleId()).toString(), positionals);
                } else {
                    positionals1i.add(positional);
                    if (positionals1i.size() > 0) {
                        if (i != positionals.size() - 1 && !UuidUtils.getUUIDFromBytes(positional.getVehicleId())
                            .toString()
                            .equals(UuidUtils.getUUIDFromBytes(positionals.get(i + 1).getVehicleId()).toString())) {
                            List<Positional> yls = new ArrayList<>();
                            yls.addAll(positionals1i);
                            listMap.put(UuidUtils.getUUIDFromBytes(positional.getVehicleId()).toString(), yls);
                            positionals1i.clear();
                        } else if (i == positionals.size() - 1) {
                            List<Positional> yls = new ArrayList<>();
                            yls.addAll(positionals1i);
                            listMap.put(UuidUtils.getUUIDFromBytes(positional.getVehicleId()).toString(), yls);
                            positionals1i.clear();
                        }
                    }
                }
            }
        }
        return listMap;
    }

    private boolean isQualified(Positional positional) {
        //定位时间错误：时间格式错误、接收时间早于定位时间，定位时间是20小时之前的数据；
        long vtime = positional.getVtime();
        String uploadTimeStr = positional.getUploadTime() != null ? positional.getUploadTime() : "0";
        long uploadTime = Long.parseLong(uploadTimeStr);
        //20小时转换成秒
        long s = 20 * 3600L;
        if (uploadTime < vtime || (uploadTime - vtime) > s) {
            return false;
        }
        //经纬度错误
        if (StringUtil.isEmpty(positional.getLongtitude()) || StringUtil.isEmpty(positional.getLatitude())) {
            return false;
        }
        double longtitude = Double.parseDouble(positional.getLongtitude());// 经度
        double latitude = Double.parseDouble(positional.getLatitude());// 纬度
        if (longtitude < 73.33 || longtitude > 135.05 || latitude < 3.51 || latitude > 53.33) {
            return false;
        }
        //海拔判断
        if (StringUtil.isEmpty(positional.getHeight()) || Double.parseDouble(positional.getHeight()) < -200
            || Double.parseDouble(positional.getHeight()) > 6000) {
            return false;
        }
        //车速
        if (StringUtil.isEmpty(positional.getSpeed()) || Double.parseDouble(positional.getSpeed()) < 0
            || Double.parseDouble(positional.getSpeed()) > 160) {
            return false;
        }
        //方向
        if (StringUtil.isEmpty(positional.getAngle()) || Double.parseDouble(positional.getAngle()) < 0
            || Double.parseDouble(positional.getAngle()) > 360) {
            return false;
        }
        return true;
    }

    @Override
    public boolean export(String title, int type, HttpServletResponse res)
        throws IOException {
        String userId = userService.getCurrentUserInfo().getId().toString();
        List<LocationQualifiedRateReport> pis =
                RedisHelper.getList(HistoryRedisKeyEnum.SX_LOCATION_QUALIFIED_RATE_REPORT_INFORMATION.of(userId),
                        LocationQualifiedRateReport.class);
        return ExportExcelUtil.export(
            new ExportExcelParam(title, type, pis, LocationQualifiedRateReport.class, null, res.getOutputStream()));
    }

}
