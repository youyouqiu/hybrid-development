package com.zw.platform.util.common;

import com.zw.platform.commons.HttpClientUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 从高德获取天气
 * Created by wjy on 2018/10/17.
 *
 * @author lj
 */
@Component
public class Weather {
    private static final Logger log = LogManager.getLogger(Weather.class);

    @Value("${api.key.gaode}")
    private String gaodeKey;

    private static String KEY;
    //用来存全国的天气
    public static Map<String, String> weatherData = new ConcurrentHashMap<>();
    //用来存取报连接超时的adcode
    public static List<String> timeOutCode = new ArrayList<>();

    @Autowired
    public void init() {
        KEY = gaodeKey;
    }

    /**
     * 逆地理编码
     *
     * @return result
     */
    public static String getWeather(String cityCode) {
        String urlNotGo = "https://restapi.amap.com/v3/weather/weatherInfo";
        try {
            Map<String, String> params = new HashMap<>(4);
            params.put("key", KEY);
            params.put("city", cityCode);
            return HttpClientUtil.doGet(urlNotGo, params);
        } catch (SocketTimeoutException e) {
            //将连接超时的code记录
            timeOutCode.add(cityCode);
        } catch (Exception ex) {
            log.info("未获取到该地区(" + cityCode + ")天气！");
        }
        return "";
    }

    public static String getAddress() {
        String url = "https://restapi.amap.com/v3/config/district";
        Map<String, String> params = new HashMap<>();
        params.put("key", KEY);
        params.put("keywords", "中国");
        params.put("subdistrict", "3");
        try {
            return HttpClientUtil.doGet(url, params);
        } catch (SocketTimeoutException e) {
            log.error("获取中国城市经纬度超时！", e);
        } catch (Exception e) {
            log.error("获取中国城市经纬度异常！", e);
        }
        return "";
    }

}
