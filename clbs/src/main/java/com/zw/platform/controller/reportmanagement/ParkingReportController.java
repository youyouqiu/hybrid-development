
package com.zw.platform.controller.reportmanagement;

import com.zw.platform.commons.Auth;
import com.zw.platform.domain.reportManagement.ParkingInfo;
import com.zw.platform.dto.reportManagement.ParkingInfoDto;
import com.zw.platform.service.monitoring.impl.RealTimeServiceImpl;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.service.reportManagement.ParkingReportService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RedisUtil;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/m/reportManagement/parkingReport")
public class ParkingReportController {
    private static final String LIST_PAGE = "modules/reportManagement/parkingReport";

    @Autowired
    private ParkingReportService parkingReportService;

    @Autowired
    RealTimeServiceImpl realTime;

    @Autowired
    private PositionalService positionalService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    private static Logger log = LogManager.getLogger(ParkingReportController.class);

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 获取停驶数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping(value = "/getStopData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSpeedAlarmList(String vehicleId, String startTime, String endTime) {
        try {
            if (StringUtils.isNotBlank(vehicleId) && StringUtils.isNotBlank(startTime)
                    && StringUtils.isNotBlank(endTime)) {
                List<String> vehicleIds = Arrays.asList(vehicleId.split(","));
                List<ParkingInfo> pis = parkingReportService.getStopData(vehicleIds, startTime, endTime);
                return new JsonResultBean(pis);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("获取停驶数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取停驶数据（大数据月表）
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping(value = "/getStopBigData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getStopBigData(String vehicleId, String startTime, String endTime) {
        try {
            if (StringUtils.isNotBlank(vehicleId) && StringUtils.isNotBlank(startTime)
                    && StringUtils.isNotBlank(endTime)) {
                List<String> vehicleIds = Arrays.asList(vehicleId.split(","));
                List<ParkingInfo> pis = parkingReportService.getStopBigData(vehicleIds, startTime, endTime, false);
                return new JsonResultBean(pis);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("获取停驶数据（大数据月表）异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取停驶数据（大数据月表，调用paas-cloud接口）
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping(value = "/getStopBigDataNew", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getStopBigDataNew(String vehicleId, String startTime, String endTime) {
        try {
            if (StringUtils.isNotBlank(vehicleId) && StringUtils.isNotBlank(startTime) && StringUtils
                .isNotBlank(endTime)) {
                List<ParkingInfoDto> piDtoList =
                    parkingReportService.getStopBigDataFromPaas(vehicleId, startTime, endTime, false);
                return new JsonResultBean(piDtoList);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("获取停驶数据（大数据月表）异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出(存数据)
     * @param vehicleId(全部车牌号)
     * @param vehicleId(全部车辆id)
     * @param startTime(开始时间)
     * @param endTime(结束时间)
     * @return boolean
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    @ResponseBody
    public boolean export1(String vehicleId, String startTime, String endTime, int exportType) {
        try {
            List<String> vehicleIds = Arrays.asList(vehicleId.split(","));
            List<ParkingInfo> pis;
            switch (exportType) {
                case 1:
                    pis = getAddress(parkingReportService.getStopData(vehicleIds, startTime, endTime));
                    break;
                case 2:
                    pis = getAddress(parkingReportService.getStopBigData(vehicleIds, startTime, endTime, false));
                    break;
                default:
                    pis = new ArrayList<>();
                    break;
            }
            RedisUtil.storeExportDataToRedis("exportStopInfo", pis);
            return true;
        } catch (Exception e) {
            log.error("导出车辆信息(post)异常", e);
            return false;
        }
    }

    /**
     * 导出(生成excel文件)
     * @param res
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export2(HttpServletResponse res) {
        try {
            ExportExcelUtil.setResponseHead(res, "停止报表");
            parkingReportService.export(null, 1, res);
        } catch (Exception e) {
            log.error(" 导出车辆信息(get)异常", e);
        }
    }

    /**
     * 获取停驶位置信息
     * @param pis
     * @return
     */
    public List<ParkingInfo> getAddress(List<ParkingInfo> pis) {
        for (ParkingInfo pi : pis) {
            String longitude = null;
            String latitude = null;
            if (StringUtils.isNotBlank(pi.getStopLocation())) {
                String[] stopLocation = pi.getStopLocation().split(",");
                longitude = stopLocation[0];
                latitude = stopLocation[1];
            }
            pi.setStopLocation(positionalService.getAddress(longitude, latitude));
        }
        return pis;
    }
}
