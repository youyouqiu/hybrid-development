package com.zw.platform.controller.mileageDetection;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.oil.PositionalQuery;
import com.zw.platform.domain.oil.PositionlList;
import com.zw.platform.domain.vas.mileageSensor.MileageSensorConfig;
import com.zw.platform.service.mileageSensor.MileageSensorConfigService;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisUtil;
import org.apache.commons.lang3.StringUtils;
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
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LiaoYuecai on 2017/5/15.
 */
@Controller
@RequestMapping("/v/meleMonitor/mileStatistics")
public class MileageStatisticsController {

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${real.mileage.null}")
    private String realMileageNull;

    @Value("${travel.mileage.null}")
    private String travelMileageNull;

    @Value("${real.mileage.error}")
    private String realMileageError;

    @Value("${travel.mileage.error}")
    private String travelMileageError;

    private static final String LIST_PAGE = "vas/meleMonitor/mileStatistics/list";

    private static final String ADD_PAGE = "vas/meleMonitor/mileStatistics/add";

    private static final String EDIT_PAGE = "vas/meleMonitor/mileStatistics/edit";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private static Logger log = LogManager.getLogger(MileageStatisticsController.class);

    @Autowired
    private MileageSensorConfigService mileageSensorConfigService;

    @Autowired
    private PositionalService positionalService;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private MileageSensorConfigService msservice;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public ModelAndView listPage() {
        try {
            ModelAndView mav = new ModelAndView(LIST_PAGE);
            // 查询参考车辆
            List<MileageSensorConfig> vehicleList = msservice.findVehicleSensorSet();
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            return mav;
        } catch (Exception e) {
            log.error("查询弹出列表异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public String addPage() throws BusinessException {
        return ADD_PAGE;
    }

    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getList(final PositionalQuery query) {
        String username = SystemHelper.getCurrentUser().getUsername();
        final RedisKey key = HistoryRedisKeyEnum.STATS_MILEAGE.of(username);
        Page<PositionlList> result = RedisUtil.queryPageList(key, HistoryRedisKeyEnum.STATS_MILEAGE_DATA::of, query);
        return new PageGridBean(query, result, true);
    }

    @RequestMapping(value = { "/getHistoryInfoByVid" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getHistoryInfoByVid(String vehicleId, String startTime, String endTime) {
        try {
            JSONObject msg = new JSONObject();
            msg = positionalService.getHistoryInfoByPaas(vehicleId, startTime, endTime, msg);
            return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());
        } catch (Exception e) {
            log.error("获取车辆信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 修正里程下发
     * @param vehicleId
     * @param endTime
     * @return
     * @throws BusinessException
     * @throws ParseException
     */
    @RequestMapping(value = { "/sendMileage" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendMileage(HttpServletRequest request, String vehicleId, String endTime, String travelMail,
        String realMail) {
        try {
            if (StringUtil.isNull(realMail)) {
                return new JsonResultBean(JsonResultBean.FAULT, realMileageNull);
            }
            if (StringUtil.isNull(travelMail)) {
                return new JsonResultBean(JsonResultBean.FAULT, travelMileageNull);
            }
            MileageSensorConfig mc = this.mileageSensorConfigService.findByVehicleId(vehicleId, true);
            Double rm = Double.parseDouble(realMail);
            Double tm = Double.parseDouble(travelMail);
            if (rm <= 0) {
                return new JsonResultBean(JsonResultBean.FAULT, realMileageError);
            }
            if (tm <= 0) {
                return new JsonResultBean(JsonResultBean.FAULT, travelMileageError);
            }
            Double d = this.reviseCoefficient(rm, tm, mc.getRollingRadius().doubleValue());
            Integer rollingRadius = d.intValue();
            // 下发
            // 判断当前车辆是否绑定参数设置
            String vechileid = mc.getVehicleId();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("vehicleId", vechileid);// 车辆编号
            map.put("send8103ParamId", mc.getSend8103ParamId());// 车辆编号
            map.put("rollingRadius", rollingRadius);// 轮播系数

            JSONObject json = this.mileageSensorConfigService.sendParam(map);
            json.put("rollingRadius", rollingRadius);
            json.put("username", SystemHelper.getCurrentUsername());
            return new JsonResultBean(JsonResultBean.SUCCESS, json.toJSONString());
        } catch (Exception e) {
            log.error("修正里程下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 修正里程下发
     * @param vehicleId
     * @param endTime
     * @return
     * @throws BusinessException
     * @throws ParseException
     */
    @RequestMapping(value = { "/updateMileage" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateMileage(HttpServletRequest request, String vehicleId, String endTime,
        String rollingRadius) {
        try {
            if (StringUtil.isNull(rollingRadius)) {
                return new JsonResultBean(JsonResultBean.FAULT, realMileageNull);
            }
            MileageSensorConfig mc = this.mileageSensorConfigService.findByVehicleId(vehicleId, true);
            Double rm = Double.parseDouble(rollingRadius);
            mc.setRollingRadius(rm.intValue());
            mc.setNominalTime(DateUtil.getStringToDate(endTime, ""));
            String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
            this.mileageSensorConfigService.updateMileageSensorConfig(mc, false, ip);
            String[] vehicle = logSearchService.findCarMsg(vehicleId);
            if (vehicle != null) {
                String brand = vehicle[0];
                String plateColor = vehicle[1];
                String msg = "监控对象：" + StringUtils.join(vehicle) + " 更新里程标定下发";
                logSearchService.addLog(ip, msg, "2", "车辆里程标定", brand, plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("修正里程下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 修正计算
     * @param actualMileage 实际里程
     * @param mileage       行驶里程
     * @param coefficient   修正前系数
     * @return 修正后系数
     */
    private Double reviseCoefficient(Double actualMileage, Double mileage, Double coefficient) {
        return (actualMileage * coefficient) / mileage;
    }
}
