package com.cb.platform.contorller;


import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.EnterpriseDispatch;
import com.cb.platform.domain.VehicleScheduler;
import com.cb.platform.service.VehicleDispatchReportService;
import com.sx.platform.contorller.sxReportManagement.BeforeDawnReportController;
import com.zw.platform.commons.Auth;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


@Controller
@RequestMapping("/cb/cbReportManagement/vehicleDispatchReport")
public class VehicleDispatchReportController {
    private static final String LIST_PAGE = "modules/cbReportManagement/vehicleDispatchReport";

    private static Logger logger = LogManager.getLogger(BeforeDawnReportController.class);

    @Autowired
    VehicleDispatchReportService vehicleDispatchReportService;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 车辆调度信息道路运输企业统计查询接口
     * @param groupList
     * @param month
     * @return
     */
    @RequestMapping(value = {"/getEnterpriseList"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getEnterpriseList(String groupList, String month) {
        try {
            JSONObject obj = new JSONObject(); // 传入JSONObject
            List<EnterpriseDispatch> list = vehicleDispatchReportService.getEnterpriseList(groupList, month);
            obj.put("list", list);
            return new JsonResultBean(obj); // 返回给页面
        } catch (Exception e) {
            logger.error("查询车辆调度信息道路运输企业统计失败", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 车辆调度信息道路运输企业统计导出接口 导出(生成excel文件)
     * @param res
     */
    @RequestMapping(value = "/exportEnterpriseList ", method = RequestMethod.GET)
    public void exportEnterpriseList(HttpServletResponse res) {
        try {
            ExportExcelUtil.setResponseHead(res, "车辆调度信息道路运输企业统计月报表");
            vehicleDispatchReportService.exportEnterpriseList(null, 1, res);
        } catch (Exception e) {
            logger.error("车辆调度信息道路运输企业统计月报表导出数据异常(get)", e);
        }
    }

    /**
     * 车辆调度信息统计月报表查询接口
     * @param vehicleList
     * @param month
     * @return
     */

    @RequestMapping(value = {"/getVehicleList"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVehicleList(String vehicleList, String month) {
        try {
            JSONObject obj = new JSONObject(); // 传入JSONObject
            List<EnterpriseDispatch> list = vehicleDispatchReportService.getVehicleList(vehicleList, month);
            obj.put("list", list);
            // 返回给页面
            return new JsonResultBean(obj);
        } catch (Exception e) {
            logger.error("查询车辆调度信息统计月报表失败", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 车辆调度信息统计月报表导出 导出(生成excel文件)
     * @param res
     */
    @RequestMapping(value = "/exportVehicleList ", method = RequestMethod.GET)
    public void exportVehicleList(HttpServletResponse res) {
        try {
            ExportExcelUtil.setResponseHead(res, "车辆调度信息统计月报表");
            vehicleDispatchReportService.exportVehicleList(null, 1, res);
        } catch (Exception e) {
            logger.error("车辆调度信息统计月报表导出数据异常(get)", e);
        }
    }

    /**
     * 车辆调度信息明细表查询接口
     * @param vehicleList
     * @param startTime
     * @param endTime
     * @return
     */

    @RequestMapping(value = {"/getDetailList"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDetailList(String vehicleList, String startTime, String endTime) {
        try {
            // 传入JSONObject
            JSONObject obj = new JSONObject();
            List<VehicleScheduler> list = vehicleDispatchReportService.getDetailList(vehicleList, startTime, endTime);
            obj.put("list", list);
            return new JsonResultBean(obj); // 返回给页面
        } catch (Exception e) {
            logger.error("查询车辆调度信息明细表失败", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 导出(生成excel文件)
     * @param res
     */
    @RequestMapping(value = "/exportDetailList", method = RequestMethod.GET)
    public void exportDetailList(HttpServletResponse res) {
        try {
            ExportExcelUtil.setResponseHead(res, "车辆调度信息统计月报表");
            vehicleDispatchReportService.exportDetailList(null, 1, res);
        } catch (Exception e) {
            logger.error("车辆调度信息明细表查询导出异常(get)", e);
        }
    }
}
