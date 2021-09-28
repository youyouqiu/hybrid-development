package com.zw.platform.controller.generalCargoReport;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.generalCargoReport.CargoOffLineReport;
import com.zw.platform.service.generalCargoReport.GeneralCargoOffLineReportService;
import com.zw.platform.util.common.CargoCommonUtils;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.TemplateExportExcel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author XK
 */
@Controller
@RequestMapping("/GeneralCargo/ReportManagement/offLineReport")
public class GeneralCargoOffLineReportController {
    private static final String LIST_PAGE = "modules/sdReportManagement/offlineVehicleDisposalRecords";

    private static Logger logger = LogManager.getLogger(GeneralCargoOffLineReportController.class);

    @Autowired
    private GeneralCargoOffLineReportService generalCargoOffLineReportService;

    @Autowired
    ServletContext servletContext;

    @Autowired
    UserService userService;

    @Autowired
    TemplateExportExcel templateExportExcel;

    private static Logger log = LogManager.getLogger(MonthReportCargoController.class);

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        String username = userService.getCurrentUserInfo().getUsername();
        RedisKey redisKey = HistoryRedisKeyEnum.CARGO_OFFLINE_REPORT_INFORMATION.of(username);
        RedisHelper.delete(redisKey);
        return LIST_PAGE;
    }

    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getListByGroupId(Integer day, String groupIds) {
        try {
            String[] groups = groupIds.split(",");
            Set<String> vehicleSet = CargoCommonUtils.getGroupCargoVids(groups);
            return getList(vehicleSet, day);
        } catch (Exception e) {
            logger.error("查询离线报表失败", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    private JsonResultBean getList(Set<String> vehicleSet, Integer day) {
        try {
            JSONObject obj = new JSONObject();
            // 查询离线报表数据
            List<CargoOffLineReport> list = generalCargoOffLineReportService.getList(vehicleSet, day);
            String username = userService.getCurrentUserInfo().getUsername();
            RedisKey redisKey = HistoryRedisKeyEnum.CARGO_OFFLINE_REPORT_INFORMATION.of(username);
            // 再次查询前删除 key
            RedisHelper.delete(redisKey);
            // 获取组装数据存入redis管道
            RedisHelper.addToList(redisKey, list);
            obj.put("list", list);
            // 返回给页面
            return new JsonResultBean(obj);
        } catch (Exception e) {
            logger.error("查询离线报表失败", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 导出(生成excel文件)
     * @param res res
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse res, String simpleQueryParam) {
        try {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy 年 MM 月 dd 日");
            String time = sdf.format(date);
            String username = userService.getCurrentUserInfo().getUsername();
            RedisKey redisKey = HistoryRedisKeyEnum.CARGO_OFFLINE_REPORT_INFORMATION.of(username);
            List<CargoOffLineReport> cargoOffLineReports = RedisHelper.getList(redisKey, CargoOffLineReport.class);
            cargoOffLineReports = generalCargoOffLineReportService.getExportList(simpleQueryParam, cargoOffLineReports);
            Map<String, Object> data = new HashMap<>(8);
            data.put("cargoOffLineReports", cargoOffLineReports);
            data.put("time", time);
            String fileName = "【" + new SimpleDateFormat("yyyyMMdd").format(date) + "】道路运输车辆动态监控离线车辆处置记录表";
            templateExportExcel.templateExportExcel(TemplateExportExcel.OFF_LINE_REPORT, res, data, fileName);
        } catch (Exception e) {
            log.error("导出普货月报表异常", e);
        }
    }
}
