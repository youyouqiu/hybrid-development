package com.sx.platform.service.sxReportManagement.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.util.page.PassCloudResultBean;
import com.sx.platform.domain.sxReport.ShiftDataReport;
import com.sx.platform.service.sxReportManagement.ShiftDataReportService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
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
 * @date 2018/3/12 11:10
 */
@Service
public class ShiftDataReportServiceImpl implements ShiftDataReportService {

    @Autowired
    UserService userService;

    @Autowired
    OrganizationService organizationService;

    @Override
    public JsonResultBean getListFromPaas(String band, String startTime, String endTime) throws Exception {
        List<ShiftDataReport> result = new ArrayList<>();
        Map<String, String> param = new HashMap<>();
        param.put("startTime", DateUtil.formatDate(startTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT));
        param.put("endTime", DateUtil.formatDate(endTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT));
        param.put("monitorIds", band);
        String sendResult = HttpClientUtil.send(PaasCloudUrlEnum.SX_SHIFT_DATA_URL, param);
        PassCloudResultBean passCloudResultBean = PassCloudResultBean.getDataInstance(sendResult);
        Object data = passCloudResultBean.getData();
        if (!passCloudResultBean.isSuccess()) {
            return new JsonResultBean(JsonResultBean.FAULT, passCloudResultBean.getMessage());
        }
        if (Objects.isNull(data)) {
            return new JsonResultBean(result);
        }
        JSONArray jsonArray = JSONObject.parseArray(data.toString());
        if (CollectionUtils.isNotEmpty(jsonArray)) {
            List<String> monitorIds = Arrays.asList(band.split(","));
            Map<String, VehicleDTO> vehicleMap = MonitorUtils.getVehicleMap(monitorIds);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                ShiftDataReport report = new ShiftDataReport();
                String monitorId = object.getString("monitorId");
                report.setPlateNumber(object.getString("monitorName"));
                report.setShiftCount(object.getInteger("shiftCount"));
                report.setProfessionalNames(object.getString("professionalsName"));
                report.setAssignmentName(object.getString("assignmentName"));
                report.setVehType(object.getString("objectType"));
                VehicleDTO vehicleDTO = vehicleMap.get(monitorId);
                report.setGroupName(vehicleDTO.getOrgName());
                report.setPlateColor(PlateColor.getNameOrBlankByCode(vehicleDTO.getPlateColor()));
                result.add(report);
            }
        }
        String userId = userService.getCurrentUserInfo().getId().toString();
        RedisKey redisKey = HistoryRedisKeyEnum.SX_SHIFT_DATA_REPORT_INFORMATION.of(userId);
        // 再次查询前删除 key
        RedisHelper.delete(redisKey);
        // 获取组装数据存入redis管道
        RedisHelper.addToList(redisKey, result);
        return new JsonResultBean(result);
    }

    @Override
    public boolean export(String title, int type, HttpServletResponse res)
        throws IOException {
        String userId = userService.getCurrentUserInfo().getId().toString();
        List<ShiftDataReport> pis =
                RedisHelper.getList(HistoryRedisKeyEnum.SX_SHIFT_DATA_REPORT_INFORMATION.of(userId),
                        ShiftDataReport.class);
        return ExportExcelUtil
            .export(new ExportExcelParam(title, type, pis, ShiftDataReport.class, null, res.getOutputStream()));
    }

}
