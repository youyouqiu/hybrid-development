package com.zw.platform.controller.monitoring;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.vas.f3.TransdusermonitorSet;
import com.zw.platform.domain.vas.monitoring.RefrigeratorForm;
import com.zw.platform.service.monitoring.HumitureService;
import com.zw.platform.service.sensorSettings.SensorSettingsService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.ZipUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;


/**
 * 温湿度监控
 *
 * @author wangying
 */
@Controller
@RequestMapping("/v/monitoring/humiture")
public class HumitureController {
    private static Logger logger = LogManager.getLogger(HumitureController.class);

    private static final String LIST_PAGE = "vas/monitoring/humiture";

    @Autowired
    private HumitureService humitureService;

    @Autowired
    private SensorSettingsService sensorSettingsService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    /**
     * 温湿度监控页面
     */
    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public ModelAndView getListPage() {
        ModelAndView mv = new ModelAndView(LIST_PAGE);
        return mv;
    }

    /**
     * 一段时间内的温湿度数据统计
     *
     * @param startTime
     * @param endTime
     * @param vehicleId
     */
    @RequestMapping(value = "/tempHum", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean tempHumStatis(String startTime, String endTime, String vehicleId, int flag) {
        try {
            if (startTime != null && endTime != null && vehicleId != null && !startTime.isEmpty()
                && !endTime.isEmpty() && !vehicleId.isEmpty()) {
                JSONObject msg = new JSONObject();
                List<RefrigeratorForm> data = humitureService.getTempDtaAndHumData(vehicleId, startTime, endTime);
                if (flag == 1) { // 需要返回传感器信息
                    List<TransdusermonitorSet> transdusermonitorSets = new ArrayList<>();
                    List<Integer> numbers = new ArrayList<>();
                    numbers.add(1); // 1 : 温度传感器
                    numbers.add(2); // 2 : 湿度传感器
                    for (Integer type : numbers) { // 根据监控对象id查询绑定的温湿度传感器信息
                        List<TransdusermonitorSet> transdusermonitorSetList =
                            sensorSettingsService.findByVehicleId(type, vehicleId);
                        transdusermonitorSets.addAll(transdusermonitorSetList);
                    }
                    msg.put("transdusermonitorSet", transdusermonitorSets);
                }
                String result = JSON.toJSONString(data);
                // 压缩数据
                result = ZipUtil.compress(result);
                msg.put("humitureWatch", result);
                return new JsonResultBean(msg);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("温湿度监控查询温湿度传感器统计异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }


    /**
     * 根据车id获取监控对象的最后一条位置信息中的温度数据
     */
    @RequestMapping(value = "/getTreeMonitorTempData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMonitorLastLocationById(String monitorId) {
        try {
            return new JsonResultBean(humitureService.getMonitorLastLocationById(monitorId));
        } catch (Exception e) {
            logger.error("温湿度监控查询监控对象树温度值数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
