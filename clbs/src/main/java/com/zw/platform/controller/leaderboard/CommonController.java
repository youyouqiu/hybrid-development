package com.zw.platform.controller.leaderboard;

import com.alibaba.fastjson.JSONArray;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.domain.oil.WeatherInfo;
import com.zw.platform.service.leaderboard.CommonService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.Weather;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公用方法
 */
@Controller
@RequestMapping("/adas/lb/common")
public class CommonController {
    private static final Logger log = LogManager.getLogger(CommonController.class);

    @Value("${adas.isVip}")
    private boolean isVip;

    @Autowired
    private CommonService commonService;

    @RequestMapping(value = "/getSelectGroups")
    @ResponseBody
    public JsonResultBean getSelectGroups() {
        try {
            return new JsonResultBean(commonService.getUserOrg());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取用户权限的企业下拉款失败！" + e);
            return new JsonResultBean(JsonResultBean.FAULT);

        }
    }

    @RequestMapping(value = "/getChinaWeather", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getChinaWeather() {

        try {
            List<WeatherInfo> weatherInfos = new ArrayList<>();
            WeatherInfo weatherInfo;
            if (Weather.weatherData.size() > 0) {

                for (String value : Weather.weatherData.values()) {
                    weatherInfo = JSONArray.parseObject(value, WeatherInfo.class);
                    if (weatherInfo.getLevel().equals("city")) {
                        weatherInfos.add(weatherInfo);
                    } else if (weatherInfo.getLevel().equals("province")) {
                        if (!weatherInfo.getWeather().equals("暂无天气信息")) {
                            weatherInfo.setLevel("city");
                            weatherInfo.setCity(weatherInfo.getProvince());
                            weatherInfos.add(weatherInfo);
                        }
                    }
                }

            } else {

                List<String> weathers = RedisHelper.hvals(HistoryRedisKeyEnum.CHINA_WEATHER_LIVE.of());
                for (String weather : weathers) {
                    weatherInfo = JSONArray.parseObject(weather, WeatherInfo.class);
                    if (weatherInfo.getLevel().equals("city")) {
                        weatherInfos.add(weatherInfo);
                    } else if (weatherInfo.getLevel().equals("province")) {
                        if (!weatherInfo.getWeather().equals("暂无天气信息")) {
                            weatherInfo.setLevel("city");
                            weatherInfo.setCity(weatherInfo.getProvince());
                            weatherInfos.add(weatherInfo);
                        }
                    }
                }
            }
            return new JsonResultBean(weatherInfos);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取全国天气失败！" + e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @RequestMapping(value = "/isVip")
    @ResponseBody
    public JsonResultBean isVip() {
        try {
            Map<String, Boolean> result = new HashMap<>();
            result.put("isVip", isVip);
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("主动安全访问控制调用失败！" + e);
            return new JsonResultBean(JsonResultBean.FAULT);

        }
    }

}
