package com.zw.platform.controller.temperatureDetection;

import com.alibaba.fastjson.JSON;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.vas.f3.TransdusermonitorSet;
import com.zw.platform.service.sensorSettings.SensorSettingsService;
import com.zw.platform.util.common.BusinessException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * 温度统计Controller
 * @author hujun 2017/7/6
 */
@Controller
@RequestMapping("/v/temperatureDetection/temperatureStatistics")
public class TemperatureStatisticsController {
    private static Logger log = LogManager.getLogger(TemperatureStatisticsController.class);
    private static final String LIST_PAGE = "vas/temperatureDetection/temperatureStatistics/list";
    private static final String ERROR_PAGE = "html/errors/error_exception";
    @Autowired
    private SensorSettingsService sensorSettingsService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public ModelAndView listPage() throws BusinessException {
        try {
            ModelAndView mav = new ModelAndView(LIST_PAGE);
            //查询参考车信息
            List<TransdusermonitorSet> vehicleList = sensorSettingsService.findVehicleReference(1);
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            return mav;
        } catch (Exception e) {
            log.error("获取温度统计信息异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }
}
