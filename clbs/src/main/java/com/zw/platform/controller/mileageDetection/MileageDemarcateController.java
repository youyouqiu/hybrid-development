package com.zw.platform.controller.mileageDetection;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.vas.mileageSensor.MileageSensorConfig;
import com.zw.platform.service.mileageSensor.MileageSensorConfigService;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by LiaoYuecai on 2017/5/16.
 */
@Controller
@RequestMapping("/v/meleMonitor/mileageDemarcate")
public class MileageDemarcateController {

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${mileage.data.null}")
    private String mileageDataNull;

    private static final String LIST_PAGE = "vas/meleMonitor/mileageDemarcate/list";

    private static final String ADD_PAGE = "vas/meleMonitor/mileageDemarcate/add";

    private static final String EDIT_PAGE = "vas/meleMonitor/mileageDemarcate/edit";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private static Logger log = LogManager.getLogger(MileageDemarcateController.class);

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    private PositionalService positionalService;

    @Autowired
    private MileageSensorConfigService mileageSensorConfigService;

    @Autowired
    private HttpServletRequest request;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public ModelAndView listPage() {
        try {
            ModelAndView mav = new ModelAndView(LIST_PAGE);
            // 查询绑定传感器车辆
            List<MileageSensorConfig> vehicleList = mileageSensorConfigService.findVehicleSensorSet();
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            return mav;
        } catch (Exception e) {
            log.error("查询弹出列表异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
        // return LIST_PAGE;
    }

    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public String addPage() throws BusinessException {
        return ADD_PAGE;
    }

    @RequestMapping(value = { "/edit" }, method = RequestMethod.GET)
    public String editPage() throws BusinessException {
        return EDIT_PAGE;
    }

    @RequestMapping(value = { "/getHistoryInfoByVid" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getHistoryInfoByVid(String vehicleId, String startTime, String endTime) {
        try {
            JSONObject msg = new JSONObject();
            List<Positional> positionals = positionalService.getHistoryInfo(vehicleId, startTime, endTime);
            MileageSensorConfig msc = mileageSensorConfigService.findByVehicleId(vehicleId);
            String mileBefore = "";// 开始里程
            String timeBefore = "";// 开始时间

            String mileAfter = "";// 结束里程
            String timeAfter = "";// 结束时间
            String minTimeAfter = "";// 最小的标定时间
            Double mile = null;
            List<Positional> tempPositionals = new ArrayList<>();
            for (Positional p : positionals) {
                if (p.getMileageTotal() == null) {
                    continue;
                }
                BigDecimal tmile = new BigDecimal(p.getMileageTotal());
                tmile = new BigDecimal(tmile.doubleValue());
                mile = tmile.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                tempPositionals.add(p);
                String time = DateFormatUtils.format(new Date(p.getVtime() * 1000), DATE_FORMAT);
                if ("".equals(mileBefore)) {
                    mileBefore = (mile) + "";
                    timeBefore = time;
                }
                mileAfter = (mile) + "";
                timeAfter = time;
                if (msc == null || msc.getNominalTime() == null) {
                    continue;
                }
                // 说明存在标定时间
                Date timeBefores = DateUtil.getStringToDate(timeBefore, "");
                try {
                    minTimeAfter = DateUtil.getDateToString(msc.getNominalTime(), "");
                    if (timeBefores.before(msc.getNominalTime())) { // 开始里程在最后标定时间之前 则调整开始里程标定时间
                        mileBefore = (mile) + "";
                        timeBefore = time;
                    }
                    Date timeAfters = DateUtil.getStringToDate(timeAfter, "");
                    if (timeAfters.before(msc.getNominalTime())) { // 结束里程在最后标定时间之前 则不能标定
                        mileAfter = "";
                        timeAfter = "";
                        mileBefore = "";
                        timeBefore = "";
                    }
                } catch (Exception e) {
                    log.error("查询图表数据异常", e);
                    return new JsonResultBean(JsonResultBean.FAULT, mileageDataNull);
                }
            }

            msg.putAll(positionalService.getStatisticalData(tempPositionals, "MILE_SENSOR"));
            msg.put("positionals", positionals);
            msg.put("isCheckConfig", false);
            if (msc != null && msc.getMileageSensorId() != null && !"".equals(msc.getMileageSensorId())) { // 说明存在标定时间
                msg.put("isCheckConfig", true);
            }
            msg.put("min_time_after", minTimeAfter);
            msg.put("mile_after", mileAfter);
            msg.put("time_after", timeAfter);
            msg.put("mile_before", mileBefore);
            msg.put("time_before", timeBefore);
            if (mileAfter.equals(mileBefore)) {
                msg.put("mile_after", "");
                msg.put("time_after", "");
            }
            return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());
        } catch (Exception e) {
            log.error("获取车辆历史信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取标定状态
     * @param vehicleId 车辆编号
     * @return
     */
    @RequestMapping(value = "/checkCalibrationStatus", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkCalibrationStatus(String vehicleId) {
        try {
            JSONObject msg = new JSONObject();
            MileageSensorConfig msc = mileageSensorConfigService.findByVehicleId(vehicleId);
            String calibrationStatus = "";
            msg.put("updateDataTime", "");
            MileageSensorConfig tempc = new MileageSensorConfig();
            tempc.setVehicleId(vehicleId);
            // 标定状态为空闲状态，可以对其进行标定，同时重置标定状态为占用状态，以防其他用户可以对当前车辆进行标定操作
            if (msc.getNominalStatus() == null || String.valueOf(msc.getNominalStatus()).equals("0")) {
                tempc.setNominalStatus(1);
                calibrationStatus = "0";
                tempc.setEnterNominalTime(new Date());
                this.mileageSensorConfigService.updateNominalStatus(tempc);
            } else {
                calibrationStatus = "1";
                // 标定状态为占用状态，获取其更新时间，用于页面判断并还原异常标定状态的数据
                Date updateDataTime = msc.getEnterNominalTime();
                if (updateDataTime != null) {
                    msg.put("updateDataTime", Converter.toString(updateDataTime, "yyyy-MM-dd HH:mm:ss"));
                }
            }
            msg.put("calibrationStatus", calibrationStatus);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("获取标定状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 更新车辆标定状态
     * @param vehicleId
     * @param calibrationStatus
     * @return JsonResultBean
     * @throws @author Liubangquan
     * @Title: updateCalibrationStatus
     */
    @RequestMapping(value = "/updateCalibrationStatus", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateCalibrationStatus(String vehicleId, String calibrationStatus) {
        try {
            MileageSensorConfig tempc = new MileageSensorConfig();
            tempc.setVehicleId(vehicleId);
            tempc.setNominalStatus(Integer.parseInt(calibrationStatus));
            // 标定状态为空闲状态，可以对其进行标定，同时重置标定状态为占用状态，以防其他用户可以对当前车辆进行标定操作
            if (Converter.toBlank(calibrationStatus).equals("1")) {
                tempc.setEnterNominalTime(new Date());
            }
            boolean success = mileageSensorConfigService.updateNominalStatus(tempc);
            if (success) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("更新车辆标定状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 判断车辆是否绑定传感器
     * @param vehicleId
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: checkIsBondOilBox
     */
    @RequestMapping(value = "/checkIsSensorConfig", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkIsBondOilBox(String vehicleId) {
        try {
            MileageSensorConfig msc = mileageSensorConfigService.findByVehicleId(vehicleId);
            if (msc != null) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("判断车辆是否绑定传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 更改标定时间
     * @param vehicleId
     * @param dateTime
     * @return
     * @throws BusinessException
     * @throws ParseException
     */
    @RequestMapping(value = { "/updateNominalTime" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateConfigTime(String vehicleId, String dateTime) {
        try {
            JSONObject msg = new JSONObject();
            MileageSensorConfig msc = mileageSensorConfigService.findByVehicleId(vehicleId);
            if (msc == null) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            msc.setNominalTime(DateUtil.getStringToDate(dateTime, ""));
            String ipAddres = new GetIpAddr().getIpAddr(request);
            mileageSensorConfigService.updateMileageSensorConfig(msc, false, ipAddres);
            return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());
        } catch (Exception e) {
            log.error("更改标定时间异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
