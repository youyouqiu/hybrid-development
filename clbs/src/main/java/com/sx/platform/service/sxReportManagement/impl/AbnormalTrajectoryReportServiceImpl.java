package com.sx.platform.service.sxReportManagement.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.util.page.PassCloudResultBean;
import com.sx.platform.domain.sxReport.AbnormalTrajectoryReport;
import com.sx.platform.service.sxReportManagement.AbnormalTrajectoryReportService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.util.MagicNumbers;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhangsq
 * @date 2018/3/12 11:09
 */
@Service
public class AbnormalTrajectoryReportServiceImpl implements AbnormalTrajectoryReportService {

    private static final Logger logger = LogManager.getLogger(AbnormalTrajectoryReportServiceImpl.class);

    @Autowired
    UserService userService;

    @Autowired
    OrganizationService organizationService;

    @Autowired
    private PositionalService positionalService;

    @Override
    public JsonResultBean getAbnormalTrajectoryFromPaas(String band, String startTime, String endTime)
        throws Exception {
        List<AbnormalTrajectoryReport> result = new ArrayList<>();
        Map<String, String> param = new HashMap<>();
        param.put("startTime", DateUtil.formatDate(startTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT));
        param.put("endTime", DateUtil.formatDate(endTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT));
        param.put("monitorIds", band);
        String sendResult = HttpClientUtil.send(PaasCloudUrlEnum.SX_ABNORMAL_TRACK_URL, param);
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
                    AbnormalTrajectoryReport report = new AbnormalTrajectoryReport();
                    report.setPlateNumber(object.getString("monitorName"));
                    report.setAssignmentName(object.getString("assignmentName"));
                    Long lostStartTime = object.getLong("lostStartTime");
                    report.setLostStartTime(DateUtil.getLongToDateStr(lostStartTime * 1000, null));
                    report.setLostStartLocation(object.getString("lostStartLocation"));
                    Long lostEndTime = object.getLong("lostEndTime");
                    report.setLostEndTime(DateUtil.getLongToDateStr(lostEndTime * 1000, null));
                    report.setLostEndLocation(object.getString("lostEndLocation"));
                    report.setLostCount(object.getLong("lostCount"));
                    report.setCompleteRate(new BigDecimal(object.getString("completeRate")).toPlainString());
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
        RedisKey redisKey = HistoryRedisKeyEnum.SX_ABNORMAL_TRAJECTORY_REPORT_INFORMATION.of(userId);
        // 再次查询前删除 key
        RedisHelper.delete(redisKey);
        // 获取组装数据存入redis管道
        RedisHelper.addToList(redisKey, result);
        return new JsonResultBean(result);
    }

    /**
     * 剔除漂移点数据
     */
    private List<Positional> removeShiftData(List<Positional> positionals1) {
        // 超速界定 m/s(大于该值为超速)
        double speedingSpeed =
            new BigDecimal(160 * 1000).divide(new BigDecimal(3600), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        //每一辆车剔除漂移点后的数据
        List<Positional> positionals2 = new ArrayList<>(positionals1);
        //获取漂移点
        if (positionals1.size() >= 3) {
            for (int i = 0; i < positionals1.size(); i++) {
                if (i < positionals1.size() - 2) {
                    double longtitude1 = Double.parseDouble(positionals1.get(i).getLongtitude());
                    double latitude1 = Double.parseDouble(positionals1.get(i).getLatitude());
                    long vtime1 = positionals1.get(i).getVtime();
                    //第二个点
                    double longtitude2 = Double.parseDouble(positionals1.get(i + 1).getLongtitude());
                    double latitude2 = Double.parseDouble(positionals1.get(i + 1).getLatitude());
                    long vtime2 = positionals1.get(i + 1).getVtime();
                    long sec = vtime2 - vtime1;//获取第一段距离之间的时间差（s）
                    //获取第一段之间的距离（m）
                    double distance1 = AddressUtil.getDistance(longtitude1, latitude1, longtitude2, latitude2);
                    if (distance1 / sec > speedingSpeed) { //判断第一段是否超速
                        //第三个点
                        double longtitude3 = Double.parseDouble(positionals1.get(i + 2).getLongtitude());
                        double latitude3 = Double.parseDouble(positionals1.get(i + 2).getLatitude());
                        long vtime3 = positionals1.get(i + 2).getVtime();
                        long sec1 = vtime3 - vtime2;//获取第二段距离之间的时间差（s）
                        double distance2 =
                            AddressUtil.getDistance(longtitude2, latitude2, longtitude3, latitude3);//获取第二段之间的距离（m）
                        if (distance2 / sec1 > speedingSpeed) { //判断第二段是否超速， 如超速则记一次漂移点
                            positionals2.remove(positionals1.get(i + 1));//如果是漂移点则删除
                        }
                    }
                }
            }
        }
        return positionals2;
    }

    /**
     * 逆地理编码
     * @param locationData 经纬度信息, 格式：经度,纬度
     * @return 地理位置
     */
    private String analyzeCoordinates(String locationData) {
        if (locationData == null) {
            return positionalService.getAddress(null, null);
        }
        String specificLocations = "";
        try {
            // 根据,号分割为经度和纬度
            String[] location = locationData.split(",");
            if (location.length == MagicNumbers.INT_TWO) {
                // 逆地理编码
                specificLocations = positionalService.getAddress(location[0], location[1]);
            }
        } catch (Exception e) {
            logger.error(locationData + "逆地理编码异常！", e);
        }

        return specificLocations;
    }

    /**
     * 导出
     */
    @Override
    public boolean export(String title, int type, HttpServletResponse res) throws Exception {
        String userId = userService.getCurrentUserInfo().getId().toString();
        List<AbnormalTrajectoryReport> exportAbnormalTrajectoryReports =
                RedisHelper.getList(HistoryRedisKeyEnum.SX_ABNORMAL_TRAJECTORY_REPORT_INFORMATION.of(userId),
                        AbnormalTrajectoryReport.class);
        exportAbnormalTrajectoryReports.forEach(o -> {
            if (StringUtils.isNotBlank(o.getLostStartLocation())) {
                o.setLostStartLocation(analyzeCoordinates(o.getLostStartLocation()));
            }
            if (StringUtils.isNotBlank(o.getLostEndLocation())) {
                o.setLostEndLocation(analyzeCoordinates(o.getLostEndLocation()));
            }
        });
        return ExportExcelUtil.export(
            new ExportExcelParam(title, type, exportAbnormalTrajectoryReports, AbnormalTrajectoryReport.class, null,
                res.getOutputStream()));
    }

}
