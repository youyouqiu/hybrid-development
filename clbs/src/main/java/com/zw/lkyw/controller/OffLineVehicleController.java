package com.zw.lkyw.controller;

import com.alibaba.fastjson.JSONObject;
import com.sx.platform.domain.sxReport.OffLineReport;
import com.sx.platform.service.sxReportManagement.OffLineReportService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.Auth;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 两客一危-离线车辆Controller
 */
@Controller
@RequestMapping("/lkyw/RealTimeMonitoring/offLineVehicleReport")
public class OffLineVehicleController {
    private static final String LIST_PAGE = "vas/lkyw/offLineVehicleReport/offLineReport";
    private static Logger logger = LogManager.getLogger(OffLineVehicleController.class);

    @Autowired
    private OffLineReportService offLineReportService;

    @Autowired
    UserService userService;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getList(String vehicleList, Integer day) {
        try {
            JSONObject obj = new JSONObject();
            // 查询离线报表数据
            List<OffLineReport> list = offLineReportService.getList(vehicleList, day);
            String username = userService.getCurrentUserInfo().getUsername();
            RedisKey redisKey = HistoryRedisKeyEnum.OFFLINE_REPORT_INFORMATION.of(username);
            // 再次查询前删除 key
            RedisHelper.delete(redisKey);
            // 获取组装数据存入redis管道
            RedisHelper.addToList(redisKey, list);
            obj.put("list", list);
            // 返回给页面
            return new JsonResultBean(obj);
        } catch (Exception e) {
            logger.error("查询离线车辆失败", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 导出(生成excel文件)
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse res, String simpleQueryParam) {
        try {
            offLineReportService.exportForLkyw(res, simpleQueryParam);
        } catch (Exception e) {
            logger.error("两客一危-离线查询报表页面导出数据异常", e);
        }
    }
}
