package com.zw.platform.controller.reportmanagement;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.reportManagement.VehicleOperationStatusReport;
import com.zw.platform.service.reportManagement.impl.VehicleOperationStatusServiceImpl;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 车辆运营状态报表controller
 */
@Controller
@RequestMapping("/m/reportManagement/vehicle/operationStatus")
public class VehicleOperationStatusController {
    public static final Logger log = LogManager.getLogger(VehicleOperationStatusController.class);

    private static final String LIST_PAGE = "modules/reportManagement/vehicleOperatingState";

    @Autowired
    private VehicleOperationStatusServiceImpl vehicleOperationStatusService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 查询车辆运营状态列表
     */
    @RequestMapping(value = "/getData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVehicleOperationInfoData(String vehicleIds) {
        try {
            List<VehicleOperationStatusReport> result =
                vehicleOperationStatusService.getVehicleOperationInfoById(vehicleIds);
            JSONObject msg = new JSONObject();
            msg.put("data", result);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询车辆运营状态列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出车辆运营状态列表
     */
    @RequestMapping(value = "/exportVehicleOperationData", method = RequestMethod.GET)
    public void exportVehicleOperationData(HttpServletResponse response, String param) {
        try {
            ExportExcelUtil.setResponseHead(response, "车辆运营状态报表");
            vehicleOperationStatusService.exportVehicleOperationData(response, param);
        } catch (Exception e) {
            log.error("导出车辆运营状态列表异常", e);
        }
    }
}
