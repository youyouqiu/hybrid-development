package com.zw.platform.domain.oil;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.util.StrUtil;
import lombok.Data;

import java.util.Optional;

/***
 @Author zhengjc
 @Date 2019/12/9 16:59
 @Description 电压信息
 @version 1.0
 **/
@Data
public class VoltageInfo {
    /**
     * 电压最高值
     */
    private String vh;
    /**
     * 电压最低值
     */
    private String vl;
    /**
     * 启动电压阈值
     */
    private String startValue;
    /**
     * 熄火电压阈值
     */
    private String stopValue;

    public static VoltageInfo getInstance(JSONObject terminalCheck) {
        VoltageInfo voltageInfo = new VoltageInfo();
        terminalCheck = Optional.ofNullable(terminalCheck).orElse(new JSONObject());
        voltageInfo.vh = getOrDefault(terminalCheck.getString("vh"));
        voltageInfo.vl = getOrDefault(terminalCheck.getString("vl"));
        voltageInfo.startValue = getOrDefault(terminalCheck.getString("startValue"));
        voltageInfo.stopValue = getOrDefault(terminalCheck.getString("stopValue"));
        return voltageInfo;
    }

    private static String getOrDefault(String val) {
        val = StrUtil.isBlank(val) ? "-" : val;
        return val.equals("6553.5") ? "-" : val;
    }
}
