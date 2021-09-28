package com.zw.platform.controller.humidity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.oil.PositionalQuery;
import com.zw.platform.domain.vas.f3.HumidityStatisics;
import com.zw.platform.domain.vas.f3.TransdusermonitorSet;
import com.zw.platform.service.humiditystatistics.HumidityStattisticalService;
import com.zw.platform.service.sensorSettings.SensorSettingsService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisUtil;
import com.zw.platform.util.common.ZipUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.List;


/**
 * 湿度统计controller
 */
@Controller
@RequestMapping("/v/humidity/statistical")
public class HumidityStatisticalController {
    @Autowired
    private HumidityStattisticalService humidityStattisticalService;

    @Autowired
    private SensorSettingsService sensorSettingsService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    private static final String LIST_PAGE = "vas/humidity/humidityStatistical/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";
    
    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    private static Logger logger = LogManager.getLogger(HumidityStatisticalController.class);

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public ModelAndView listPage() {
        try {
            ModelAndView mav = new ModelAndView(LIST_PAGE);
            // 查询绑定传感器的人、车、物
            List<TransdusermonitorSet> vehicleList = sensorSettingsService.findVehicleReference(2);
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            return mav;
        } catch (Exception e) {
            logger.error("获取传感器统计页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getList(final PositionalQuery query, String band, String startTime, String endTime) {
        try {
            if (!"".equals(startTime) && !"".equals(endTime)) {
                long stime = DateUtils.parseDate(startTime, DATE_FORMAT).getTime() / 1000;
                long etime = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
                final RedisKey redisKey = HistoryRedisKeyEnum.STATS_HUM.of(band, stime, etime, "");
                final List<HumidityStatisics> result = RedisHelper.getListObj(
                        redisKey, query.getStart() + 1, query.getStart() + query.getLimit());
                Page<HumidityStatisics> results = RedisUtil.queryPageList(result, query, redisKey);
                return new PageGridBean(query, results, true);
            }
        } catch (Exception e) {
            logger.error("分页查询异常", e);
            return new PageGridBean(false);
        }
        return null;
    }

    // 高湿度数据
    @RequestMapping(value = "/highList", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getHighList(final PositionalQuery query, String band, String startTime, String endTime) {
        try {
            if (!"".equals(startTime) && !"".equals(endTime)) {
                long stime = DateUtils.parseDate(startTime, DATE_FORMAT).getTime() / 1000;
                long etime = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
                final RedisKey redisKey = HistoryRedisKeyEnum.STATS_HUM.of(band, stime, etime, "-h");
                final List<HumidityStatisics> result = RedisHelper.getListObj(
                        redisKey, query.getStart() + 1, query.getStart() + query.getLimit());
                Page<HumidityStatisics> results = RedisUtil.queryPageList(result, query, redisKey);
                return new PageGridBean(query, results, true);
            }
        } catch (Exception e) {
            logger.error("分页查询异常", e);
            return new PageGridBean(false);
        }
        return null;
    }

    // 低湿度数据
    @RequestMapping(value = "/lowList", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getLowList(final PositionalQuery query, String band, String startTime, String endTime) {
        try {
            if (!"".equals(startTime) && !"".equals(endTime)) {
                long stime = DateUtils.parseDate(startTime, DATE_FORMAT).getTime() / 1000;
                long etime = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
                final RedisKey redisKey = HistoryRedisKeyEnum.STATS_HUM.of(band, stime, etime, "-l");
                final List<HumidityStatisics> result = RedisHelper.getListObj(
                        redisKey, query.getStart() + 1, query.getStart() + query.getLimit());
                Page<HumidityStatisics> results = RedisUtil.queryPageList(result, query, redisKey);
                return new PageGridBean(query, results, true);
            }
        } catch (Exception e) {
            logger.error("分页查询异常", e);
            return new PageGridBean(false);
        }
        return null;
    }

    @ResponseBody
    @RequestMapping(value = "/humidityStatisics", method = RequestMethod.POST)
    public JsonResultBean findhumidityStatisics(String startTime, String endTime, String band) {
        try {
            JSONObject msg = new JSONObject();
            List<HumidityStatisics> humidityStatisicsList = null;
            if (!"".equals(startTime) && !"".equals(endTime) && !"".equals(band)) {
                humidityStatisicsList = humidityStattisticalService.findHumidityByVehicleId(startTime, endTime, band);
                Collections.reverse(humidityStatisicsList);
            }
            String result = JSON.toJSONString(humidityStatisicsList);
            // 压缩数据
            result = ZipUtil.compress(result);
            msg.put("humidityStatisicsList", result);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            logger.error("湿度传感器统计获取传感器湿度数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
