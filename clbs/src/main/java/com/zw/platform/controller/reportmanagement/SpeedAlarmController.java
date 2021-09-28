package com.zw.platform.controller.reportmanagement;

import com.zw.platform.commons.Auth;
import com.zw.platform.domain.reportManagement.SpeedAlarm;
import com.zw.platform.service.reportManagement.SpeedAlarmService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RedisUtil;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.collections.CollectionUtils;
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
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/m/reportManagement/speedAlarmReport")
public class SpeedAlarmController {

    private static final String LIST_PAGE = "modules/reportManagement/speedAlarmReport";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private SpeedAlarmService speedAlarmService;

    private static Logger log = LogManager.getLogger(SpeedAlarmController.class);

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/getSpeedAlarmListByF3Pass", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSpeedAlarmListByF3Pass(String vehicleId, String startTime, String endTime) {
        try {
            List<SpeedAlarm> speed = new ArrayList<>();
            if (StringUtils.isNotBlank(vehicleId) && StringUtils.isNotBlank(startTime)
                    && StringUtils.isNotBlank(endTime)) {
                speed = speedAlarmService.getSpeedAlarmListByF3Pass(vehicleId, startTime, endTime);
            }
            return new JsonResultBean(speed);
        } catch (Exception e) {
            log.error("获得警报信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出(存数据)
     *
     * @param vehicleList(全部车牌号)
     * @param vehicleId(全部车辆id)
     * @param startTime(开始时间)
     * @param endTime(结束时间)
     * @throws ParseException
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean export1(String vehicleList, String vehicleId, String startTime, String endTime) {
        try {
            List<SpeedAlarm> speedAlarms = speedAlarmService.getSpeedAlarmListByF3Pass(vehicleId, startTime, endTime);
            if (CollectionUtils.isEmpty(speedAlarms)) {
                return new JsonResultBean(JsonResultBean.FAULT, "导出数据为空");
            }
            RedisUtil.storeExportDataToRedis("exportSpeedInformation", speedAlarms);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("超速报警统计页面导出超速报警统计列表(post)异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "导出失败");
        }
    }

    /**
     * 导出(生成excel文件)
     *
     * @param res
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export2(HttpServletResponse res) {
        try {
            ExportExcelUtil.setResponseHead(res, "超速报警统计列表");
            speedAlarmService.export(null, 1, res, RedisUtil.getExportDataFromRedis("exportSpeedInformation"));
        } catch (Exception e) {
            log.error("超速报警统计页面导出超速报警统计列表(get)异常", e);
        }
    }
}
