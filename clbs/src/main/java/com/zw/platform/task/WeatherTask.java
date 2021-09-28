package com.zw.platform.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.domain.oil.WeatherInfo;
import com.zw.platform.util.common.Weather;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

/**
 * @author Administrator
 */
public class WeatherTask implements Job {
    private static final Logger log = LogManager.getLogger(WeatherTask.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ApplicationContext appCtx = null;
        try {
            appCtx = (ApplicationContext) context.getScheduler().getContext().get("applicationContextKey");
            if (Objects.nonNull(appCtx)) {
                Weather.timeOutCode.clear();
                String address = "";
                address = RedisHelper.getString(HistoryRedisKeyEnum.CHINA_ADDRESS.of());
                if (address == null || "".equals(address)) {
                    int times = 0;
                    while ((address == null || "".equals(address)
                        || JSON.parseObject(address).getJSONArray("districts").size() == 0) && times < 5) {
                        address = Weather.getAddress();
                        times++;
                    }
                    if (address != null && !"".equals(address)
                        && JSON.parseObject(address).getJSONArray("districts").size() > 0) {
                        RedisHelper.setString(HistoryRedisKeyEnum.CHINA_ADDRESS.of(), address);
                    }
                }
                String province;
                String city;
                String district;
                String center;
                String level;
                String adcode;
                JSONObject jsonObject = JSON.parseObject(address);
                JSONArray districts = jsonObject.getJSONArray("districts").getJSONObject(0).getJSONArray("districts");
                log.info("开始从高德api获取全国天气");
                for (Object obj : districts) {
                    JSONObject provinceInfo = JSONObject.parseObject(obj.toString());
                    JSONArray cityInfos = provinceInfo.getJSONArray("districts");
                    province = provinceInfo.getString("name");
                    center = provinceInfo.getString("center");
                    adcode = provinceInfo.getString("adcode");
                    level = provinceInfo.getString("level");
                    getWeather(adcode, province, "", "", center, level);
                    for (Object obj1 : cityInfos) {
                        JSONObject cityInfo = JSONObject.parseObject(obj1.toString());
                        level = cityInfo.getString("level");
                        if (!"street".equals(level)) {
                            city = cityInfo.getString("name");
                            center = cityInfo.getString("center");
                            adcode = cityInfo.getString("adcode");
                            getWeather(adcode, province, city, "", center, level);
                            JSONArray districtInfos = cityInfo.getJSONArray("districts");
                            for (Object obj2 : districtInfos) {
                                JSONObject districtInfo = JSONObject.parseObject(obj2.toString());
                                level = districtInfo.getString("level");
                                if (!"street".equals(level)) {
                                    district = districtInfo.getString("name");
                                    center = districtInfo.getString("center");
                                    adcode = districtInfo.getString("adcode");
                                    getWeather(adcode, province, city, district, center, level);
                                }
                            }
                        }
                    }
                }
                log.info("开始将天气情况存入redis");
                RedisHelper.addToHash(HistoryRedisKeyEnum.CHINA_WEATHER_LIVE.of(), Weather.weatherData);
            }
        } catch (Exception e) {
            log.error("获取天气异常！", e);
        }
    }

    public boolean getWeather(String sendCode, String province, String city, String district, String center,
        String level) {
        String json = Weather.getWeather(sendCode);
        if (json != null && !"".equals(json)) {
            JSONObject jsonObject = JSON.parseObject(json);
            JSONArray lives = jsonObject.getJSONArray("lives");
            if (lives.get(0).toString() != null && !"".equals(lives.get(0).toString())) {
                WeatherInfo weatherInfo = JSONObject.parseObject(lives.get(0).toString(), WeatherInfo.class);
                if (weatherInfo != null) {
                    weatherInfo.setCenter(center);
                    weatherInfo.setProvince(province);
                    weatherInfo.setCity(city);
                    weatherInfo.setDistrict(district);
                    weatherInfo.setLevel(level);
                    Weather.weatherData.put(sendCode, JSON.toJSONString(weatherInfo));
                    return true;
                } else {
                    weatherInfo = new WeatherInfo();
                    weatherInfo.setCenter(center);
                    weatherInfo.setProvince(province);
                    weatherInfo.setCity(city);
                    weatherInfo.setDistrict(district);
                    weatherInfo.setLevel(level);
                    weatherInfo.setAdcode(sendCode);
                    weatherInfo.setWeather("暂无天气信息");
                    Weather.weatherData.put(sendCode, JSON.toJSONString(weatherInfo));
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
