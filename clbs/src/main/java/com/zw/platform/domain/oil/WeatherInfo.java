package com.zw.platform.domain.oil;


import lombok.Data;

/**
 * 天气信息实体
 */
@Data
public class WeatherInfo {
    private String province;//省
    private String city;//市
    private String district;//区
    private String weather;//天气
    private String adcode;//
    private String temperature;//温度
    private String winddirection;//风向
    private String windpower;//风级
    private String humidity;//
    private String reporttime;//发布时间
    private String center;//经纬度
    private String level;
}
